package service

import (

    "github.com/sirupsen/logrus"
)

// Engages the RESTful API
func Engage(logger *logrus.Logger) (merr error) {


	defer func() {

		if r := recover(); r != nil {
			merr = r.(error)
		}
	}()

        panic("whooo!!")
}
