<?php

declare(strict_types=1);

class LolaClient
{
    static $supportedCompanies = array(
        "TIR" => array(
            "host" => "lola.tir.uki"
        ),
        "TQ" => array(
            "host" => "lola.tq.uki"
        )
    );

    static $supportedDocs = array("fac");
    static $user = "lola";
    static $pass = "latrailera";

    protected $company;

    function __construct(string $co, string $purpose)
    {
        if (!array_key_exists($co, self::$supportedCompanies)) {
            $errMsg = "Unsupported company by this client";
            throw new Exception($errMsg);
        }

        if (!in_array($purpose, self::$supportedDocs)) {
            $errMsg = "Unsupported document to issue";
            throw new Exception($errMsg);
        }

        $this->company = $co;
        $this->purpose = $purpose;
    }

    private function logIn(): string
    {
        $ch = curl_init();

        $reqTemplate = 'http://$host/api/auth/v1/sso/token-auth';
        $targetUrl = strtr($reqTemplate, array(
            '$host' => self::$supportedCompanies[$this->company]['host']
        ));
        $payload = json_encode(array(
            'username' => self::$user,
            'password' => self::$pass
        ));

        curl_setopt($ch, CURLOPT_POSTFIELDS, $payload);
        curl_setopt($ch, CURLOPT_URL, $targetUrl);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_HTTPHEADER, array(
            'Content-Type:application/json'
        ));

        $res = curl_exec($ch);
        curl_close($ch);

        if (strlen($res) > 0) {
            $d = json_decode($res, true);
            return $d['token'];
        }

        throw new Exception('Login Fail');
    }

    private function logOut()
    {
        return;
    }

    public function doIssue(string $issueFilePath)
    {
        $token = $this->logIn();
        $this->postTokensDocument($issueFilePath, $token);
        $this->logOut();
    }

    private function postTokensDocument(string &$issueFilePath, string &$token)
    {
        if (!is_file($issueFilePath)) {
            $errMsg =
                "An issue is practically imposible without a request file";
            throw new Exception($errMsg);
        }

        $cf = new CURLFile($issueFilePath);
        $ch = curl_init();

        $reqTemplate = 'http://$host/api/issue/$purpose';
        $targetUrl = strtr($reqTemplate, array(
            '$host' => self::$supportedCompanies[$this->company]['host'],
            '$purpose' => $this->purpose
        ));

        curl_setopt($ch, CURLOPT_URL, $targetUrl);
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_POSTFIELDS, ["tokensDoc" => $cf]);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

        $result = curl_exec($ch);

        curl_close($ch);
    }
}

class FactClient extends LolaClient
{
    function __construct(string &$co)
    {
        parent::__construct($co, 'fac');
    }
}

?>
