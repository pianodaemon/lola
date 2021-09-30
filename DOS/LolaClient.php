<?php

declare(strict_types=1);

abstract class LolaClient
{
    static $supportedCompanies = [
        "TIR" => [
            "host" => "lola.tir.uki",
        ],
        "TQ" => [
            "host" => "lola.tq.uki",
        ],
    ];

    static $supportedDocs = ["fac"];
    static $userSso = "lola";
    static $passSso = "latrailera";

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
        $token = "blabla";
        if (strlen($token) > 0) {
            return token;
        }

        throw new Exception("Login Fail");
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

        // gear up options previous triggering
        $reqTemplate = 'http://$host/api/issue/$purpose';
        $targetUrl = strtr($reqTemplate, [
            '$host' => self::$supportedCompanies[$this->company]["host"],
            '$purpose' => $this->purpose,
        ]);

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
        parent::__construct($co, "fac");
    }
}

?>
