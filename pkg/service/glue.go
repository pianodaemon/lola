package service

import (
	"github.com/kelseyhightower/envconfig"
	"github.com/sirupsen/logrus"

	"immortalcrab.com/e-receipt/internal/rsapi"
)

var apiSettings rsapi.RestAPISettings

func init() {

	envconfig.Process("rsapi", &apiSettings)
}

// Engages the RESTful API
func Engage(logger *logrus.Logger) (merr error) {

	defer func() {

		if r := recover(); r != nil {
			merr = r.(error)
		}
	}()

	panic("whooo!!")
}
