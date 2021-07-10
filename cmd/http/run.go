package main

import (
    "fmt"
    "syscall"
"io/ioutil"
    "gopkg.in/yaml.v2"
)

type ConfigProfile struct {
	PAC []struct {
			Name  string `yaml:"name"`
			Value string `yaml:"value"`
		} `yaml:"pac"`
	ACL []struct {
		User  string `yaml:"user"`
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

func main() {
    var c ConfigProfile
    c.getConf()

    fmt.Println(c)

    syscall.Exit(0)
}
