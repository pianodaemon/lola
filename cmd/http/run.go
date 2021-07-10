package main

import (
        "flag"
	"fmt"
	"io/ioutil"
	"os"
	"strconv"
	"syscall"

	"github.com/sirupsen/logrus"
	"gopkg.in/yaml.v2"

	platform "immortalcrab.com/e-receipt/pkg/service"
)

const appName = "e-receipt"
const release = "flashdance"

var pidFile string
var logger *logrus.Logger

type ConfigProfile struct {
	PAC []struct {
		Name  string `yaml:"name"`
		Value string `yaml:"value"`
	} `yaml:"pac"`
	ACL []struct {
		User   string `yaml:"user"`
		Passwd string `yaml:"passwd"`
	} `yaml:"acl"`
	ResDirs []struct {
		Name  string `yaml:"name"`
		Value string `yaml:"value"`
	} `yaml:"res_dirs"`
}

func (c *ConfigProfile) getConf() *ConfigProfile {

	yamlFile, err := ioutil.ReadFile("config.yaml")
	if err != nil {
		fmt.Printf("yamlFile.Get err   #%v ", err)
	}
	err = yaml.Unmarshal(yamlFile, c)
	if err != nil {
		fmt.Printf("Unmarshal: %v", err)
	}

	return c
}

func writePidFile() error {
	if piddata, err := ioutil.ReadFile(pidFile); err == nil {
		if pid, err := strconv.Atoi(string(piddata)); err == nil {
			if process, err := os.FindProcess(pid); err == nil {
				/* If sig is 0, then no signal is sent,
				   but error checking is still performed.
				   Trick applied to check for the existence
				   of a process ID or process group ID. */
				if err := process.Signal(syscall.Signal(0)); err == nil {
					return fmt.Errorf("pid already running: %d", pid)
				}
			}
		}
	}
	/* If we get here, then the pidfile didn't exist,
	   or the pid in it doesn't belong to the user running this app.*/
	return ioutil.WriteFile(pidFile, []byte(fmt.Sprintf("%d", syscall.Getpid())), 0664)
}

func main() {

	defaultPidFile := fmt.Sprintf("/run/user/%d/%s.pid", syscall.Getuid(), appName)

	flag.StringVar(&pidFile, "pid-file", defaultPidFile, "The pathname of the process ID file.")

	flag.Parse()

	/* Write a pid file, but first make sure
	   it doesn't exist with a running pid. */
	if err := writePidFile(); err != nil {
		panic(err)
	}

	logger = logrus.New()

	logger.Out = os.Stdout
	logger.Formatter = &logrus.JSONFormatter{}
	logger.Level = logrus.InfoLevel

	logger.Printf("Engaging %s release (%s)", appName, release)
	if err := platform.Engage(logger); err != nil {
		logger.Fatalf("%s service struggles with (%v)\n", appName, err)
	}

	if err := syscall.Unlink(pidFile); err != nil {
		panic(err)
	}

	var c ConfigProfile
	c.getConf()
	fmt.Println(c)

	syscall.Exit(0)
}
