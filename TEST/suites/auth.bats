#!/usr/bin/env bats

load helpers/sso

# Here we should spin up the infra
setup() {


}

teardown() {

}

@test "login - logout" {

    run sso_login "quintanilla" "1234qwer"
    echo "output = ${output}"
    [ "$status" -eq 0 ]

    token=${output}
    run sso_logout $token
    echo "output = ${output}"
    [ "$status" -eq 0 ]
}
