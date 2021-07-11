package storage

import (
	"fmt"
	"io/ioutil"
	"os"

	"gopkg.in/yaml.v2"
)

type (
	ConfigProfile struct {
		PAC []struct {
			Name  string `yaml:"name"`
			Value string `yaml:"value"`
		} `yaml:"pac"`
		ACL []struct {
			Uid    string `yaml:"uid"`
			User   string `yaml:"user"`
			Passwd string `yaml:"passwd"`
		} `yaml:"acl"`
		ResDirs []struct {
			Name  string `yaml:"name"`
			Value string `yaml:"value"`
		} `yaml:"res_dirs"`
	}

	User struct {
		UID       string
		Username  string
		IsActive  bool
		CreatedAt int64
	}
)

var Profile *ConfigProfile

func init() {

	Profile := &ConfigProfile{}

	if err := Profile.populate(); err != nil {

		panic(err)
	}
}

func getProfile() string {

	if value, ok := os.LookupEnv("ERECEIPT_PROFILE"); ok {

		return value
	}

	return "config.yaml"
}

func (self *ConfigProfile) populate() error {

	confFile, err := ioutil.ReadFile(getProfile())

	if err != nil {

		return fmt.Errorf("Issues when opening file of profile: %v", err)
	}

	err = yaml.Unmarshal(confFile, self)

	if err != nil {

		return fmt.Errorf("Issues when parsing file of profile: %v", err)
	}

	return err
}

func Authenticate(username, password string) (*User, error) {

	for i := 0; i < len(Profile.ACL); i++ {

		if username == Profile.ACL[i].User && Profile.ACL[i].Passwd == password {

			return &User{
				UID:       Profile.ACL[i].Uid,
				Username:  Profile.ACL[i].User,
				IsActive:  true,
				CreatedAt: 12123123,
			}, nil
		}
	}

	return nil, fmt.Errorf("Verify your credentials")
}
