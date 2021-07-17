HELPERS_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

source $HELPERS_DIR/misc.bash

# authenticate and obtain a token.
sso_login() {

    local ep="$SSO_URL_BASE/token-auth"

    local res=$(curl -s -X POST -H "Content-Type: application/json" \
	             --data "{\"username\":\"$1\", \"password\":\"$2\"}" $ep)

    echo $res | grep -Fq 'token'
    if [[ $? != 0 ]]; then
        echo_err "Incorrect credentials"
    fi

    echo $res | jq -r '.token'
}


sso_logout() {

    local ep="$SSO_URL_BASE/logout"

    local res=$(curl -H 'Accept: application/json' \
                     -H "Authorization: Bearer $1" $ep)

    echo $res
}

