#!/usr/bin/env bats

load helpers/sso

# Here we should spin up the infra
setup() {

   echo "setup"
}

teardown() {
   echo "teardown"
}

@test "login - logout" {

    run sso_login "lola" "latrailera"
    echo "output = ${output}"
    [ "$status" -eq 0 ]

    # token is expected as the output from prior command
    run sso_logout ${output}
    echo "output = ${output}"
    [ "$status" -eq 0 ]
}
