<?php

abstract class LolaClient {

    static $supportedCompanies = array("TIR", "TQ");
    static $userSso = "lola";
    static $passSso = "latrailera";

    protected $company;

    function __construct(string &$co) {

        if ( !in_array($co, self::$supportedCompanies) ) {

            $errMsg = "Error: No esta configurada su company para timbrar\r Verifiquelo con el Administrador";
            exit($errMsg);
        }

        $this->company= $co;
    }

    abstract public function reqIssue(string &$req);
}


class FactClient extends LolaClient {

    public function reqIssue(string &$req) {
        echo "REQ:" . $req;
    }

    function __construct(string &$co) {

        parent::__construct($co);
    }
}

?>
