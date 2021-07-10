package service

import (
	"crypto/rsa"

	"github.com/kelseyhightower/envconfig"
	"github.com/sirupsen/logrus"

	"immortalcrab.com/e-receipt/internal/rsapi"
	ton "immortalcrab.com/e-receipt/internal/token"
)

var apiSettings rsapi.RestAPISettings

func init() {

	envconfig.Process("rsapi", &apiSettings)
}

func getExpDelta() int {

	ref := struct {
		Delta int `default:"72"`
	}{0}

	/* It stands for
	   TOKEN_CLERK_EXP_DELTA */
	envconfig.Process("token_clerk_exp", &ref)

	return ref.Delta
}

func getKeys() (*rsa.PrivateKey, *rsa.PublicKey, error) {

	ref := struct {
		Private string `default:"/pem/private_key"`
		Public  string `default:"/pem/public_key.pub"`
	}{"", ""}

	/* It stands for
	   TOKEN_CLERK_RSA_PRIVATE and  TOKEN_CLERK_RSA_PUBLIC */
	envconfig.Process("token_clerk_rsa", &ref)

	return ton.GetPrivateKey(ref.Private), ton.GetPublicKey(ref.Public), nil
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
