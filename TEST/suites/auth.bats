#!/usr/bin/env bats

load helpers/sso

# Here we should spin up the infra
setup() {

   echo "setup"
}

teardown() {
   echo "teardown"
}

@test "regular cycle login/logout" {

    run sso_login "lola" "latrailera"
    echo "output = ${output}"
    [ "$status" -eq 0 ]

    # token is expected as the output from prior command
    run sso_logout ${output}
    echo "output = ${output}"
    [ "$status" -eq 0 ]
}

@test "token blacklisted" {

    run sso_login "lola" "latrailera"
    echo "output = ${output}"
    [ "$status" -eq 0 ]

    # token is expected as the output from prior command
    run sso_logout ${output}
    echo "output = ${output}"
    [ "$status" -eq 0 ]

    # Token should have been blacklisted at this point
    run sso_logout $token
    echo "output = ${output}"
    [ $status = 1 ]
}
