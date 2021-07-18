HELPERS_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

source $HELPERS_DIR/misc.bash

# authenticate and obtain a token.
sso_login() {

    local ep="$SSO_URL_BASE/token-auth"

    local res=$(curl -s -X POST -H "Content-Type: application/json" \
	             --data "{\"username\":\"$1\", \"password\":\"$2\"}" $ep)

    echo $res | grep -Fq 'token'
    if [[ $? != 0 ]]; then
        echo_err "login fail: incorrect credentials"
    fi

    echo $res | jq -r '.token'
}

# Carry out a logout if a valid token has been provided
sso_logout() {

    local ep="$SSO_URL_BASE/logout"
    local http_status=$(curl -s -o /dev/null -w "%{http_code}"   \
                                 -H 'Accept: application/json'    \
                                 -H "Authorization: Bearer $1" $ep)

    [ $http_status = 200 ] || echo_err "logout fail: http code ${http_status}"
}


