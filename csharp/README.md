# Org.OpenAPITools - the C# library for the Deribit API

#Overview  

Deribit provides three different interfaces to access the API:

  * [JSON-RPC over Websocket](#json-rpc)
  * [JSON-RPC over HTTP](#json-rpc)
  * [FIX](#fix-api) (Financial Information eXchange)

With the API Console you can use and test the JSON-RPC API, both via HTTP and  via Websocket. To visit the API console, go to __Account > API tab >  API Console tab.__ 

## Naming

Deribit tradeable assets or instruments use the following system of naming:  

|Kind|Examples|Template|Comments|
|- -- -|- -- -- -- -|- -- -- -- -|- -- -- -- -| 
|Future|<code>BTC-25MAR16</code>, <code>BTC-5AUG16</code>|<code>BTC-DMMMYY</code>|<code>BTC</code> is currency, <code>DMMMYY</code> is expiration date, <code>D</code> stands for day of month (1 or 2 digits), <code>MMM</code> - month (3 first letters in English), <code>YY</code> stands for year.| | 
|Perpetual|<code>BTC-PERPETUAL</code>                        | |Perpetual contract for currency <code>BTC</code>.| 
|Option|<code>BTC-25MAR16-420-C</code>, <code>BTC-5AUG16-580-P</code>|<code>BTC-DMMMYY-STRIKE-K</code>|<code>STRIKE</code> is option strike price in USD. Template <code>K</code> is option kind: <code>C</code> for call options or <code>P</code> for put options.|

# JSON-RPC 
JSON-RPC is a light-weight remote procedure call (RPC) protocol. The  [JSON-RPC specification](https://www.jsonrpc.org/specification) defines the data structures that are used for the messages that are exchanged between client and server, as well as the rules around their processing. JSON-RPC uses JSON (RFC 4627) as data format.  JSON-RPC is transport agnostic: it does not specify which transport mechanism must be used. The Deribit API supports both Websocket (preferred) and HTTP (with limitations: subscriptions are not supported over HTTP).  

## Request messages 

> An example of a request message:  
```json
{     \"jsonrpc\": \"2.0\",     \"id\": 8066,     \"method\": \"public/ticker\",     \"params\": {         \"instrument\": \"BTC-24AUG18-6500-P\"     } } 
```  
According to the JSON-RPC sepcification the requests must be JSON objects with the following fields.  

|Name|Type|Description|
|- -- -|- -- -|- -- -- -- -- --|
|jsonrpc|string|The version of the JSON-RPC spec: \"2.0\"|
|id|integer or string|An identifier of the request. If it is included, then the response will contain the same identifier|
|method|string|The method to be invoked|
|params|object|The parameters values for the method. The field names must match with the expected parameter names. The parameters that are expected are described in the documentation for the methods, below.|  

<aside class=\"warning\">
The JSON-RPC specification describes two features that are currently not supported by the API:  
<ul> 
<li>Specification of parameter values by position</li> 
<li>Batch requests</li> 
</ul>  
</aside>   

## Response messages 
> An example of a response message:  
```json
{     \"jsonrpc\": \"2.0\",     \"id\": 5239,     \"testnet\": false,     \"result\": [         {             \"currency\": \"BTC\",             \"currencyLong\": \"Bitcoin\",             \"minConfirmation\": 2,             \"txFee\": 0.0006,             \"isActive\": true,             \"coinType\": \"BITCOIN\",             \"baseAddress\": null         }     ],     \"usIn\": 1535043730126248,     \"usOut\": 1535043730126250,     \"usDiff\": 2 } 
```  
The JSON-RPC API always responds with a JSON object with the following fields.   

|Name|Type|Description| 
|- -- -|- -- -|- -- -- -- -- --| 
|id|integer|This is the same id that was sent in the request.| 
|result|any|If successful, the result of the API call. The format for the result is described with each method.| |error|error object|Only present if there was an error invoking the method. The error object is described below.| 
|testnet|boolean|Indicates whether the API in use is actually the test API.  <code>false</code> for production server, <code>true</code> for test server.| 
|usIn|integer|The timestamp when the requests was received (microseconds since the Unix epoch)| |usOut|integer|The timestamp when the response was sent (microseconds since the Unix epoch)| 
|usDiff|integer|The number of microseconds that was spent handling the request|  

<aside class=\"notice\"> 
The fields <code>testnet</code>, <code>usIn</code>, <code>usOut</code> and <code>usDiff</code> are not part of the JSON-RPC standard.  <p>In order not to clutter the examples they will generally be omitted from the example code.</p> </aside>  

> An example of a response with an error:  

```json 
{     \"jsonrpc\": \"2.0\",     \"id\": 8163,     \"error\": {         \"code\": 11050,         \"message\": \"bad_request\"     },     \"testnet\": false,     \"usIn\": 1535037392434763,     \"usOut\": 1535037392448119,     \"usDiff\": 13356 } 
``` 

In case of an error the response message will contain the error field, with as value an object with the following with the following fields:  

|Name|Type|Description
|- -- -|- -- -|- -- -- -- -- --|
|code|integer|A number that indicates the kind of error.|
|message|string|A short description that indicates the kind of error.|
|data|any|Additional data about the error. This field may be omitted.|

## Notifications  
> An example of a notification:  
```json 
{     \"jsonrpc\": \"2.0\",     \"method\": \"subscription\",     \"params\": {         \"channel\": \"deribit_price_index.btc_usd\",         \"data\": {             \"timestamp\": 1535098298227,             \"price\": 6521.17,             \"index_name\": \"btc_usd\"         }     } } 
```

API users can subscribe to certain types of notifications. This means that they will receive JSON-RPC notification-messages from the server when certain events occur, such as changes to the index price or changes to the order book for a certain instrument.   The API methods [public/subscribe](#public-subscribe) and [private/subscribe](#private-subscribe) are used to set up a subscription. Since HTTP does not support the sending of messages from server to client, these methods are only availble when using the Websocket transport mechanism.  At the moment of subscription a \"channel\" must be specified. The channel determines the type of events that will be received.  See [Subscriptions](#subscriptions) for more details about the channels.  In accordance with the JSON-RPC specification, the format of a notification  is that of a request message without an <code>id</code> field. The value of the <code>method</code> field will always be <code>\"subscription\"</code>. The <code>params</code> field will always be an object with 2 members: <code>channel</code> and <code>data</code>. The value of the <code>channel</code> member is the name of the channel (a string). The value of the <code>data</code> member is an object that contains data  that is specific for the channel.   

## Authentication  

> An example of a JSON request with token:  
```json 
{     \"id\": 5647,     \"method\": \"private/get_subaccounts\",     \"params\": {         \"access_token\": \"67SVutDoVZSzkUStHSuk51WntMNBJ5mh5DYZhwzpiqDF\"     } } 
```  

The API consists of `public` and `private` methods. The public methods do not require authentication. The private methods use OAuth 2.0 authentication. This means that a valid OAuth access token must be included in the request, which can get achived by calling method [public/auth](#public-auth).  When the token was assigned to the user, it should be passed along, with other request parameters, back to the server:  

|Connection type|Access token placement 
|- -- -|- -- -- -- -- --| 
|**Websocket**|Inside request JSON parameters, as an `access_token` field| 
|**HTTP (REST)**|Header `Authorization: bearer ```Token``` ` value|  

### Additional authorization method - basic user credentials  

<span style=\"color:red\"><b> ! Not recommended - however, it could be useful for quick testing API</b></span></br>  
Every `private` method could be accessed by providing, inside HTTP `Authorization: Basic XXX` header, values with user `ClientId` and assigned `ClientSecret` (both values can be found on the API page on the Deribit website) encoded with `Base64`:  
<code>Authorization: Basic BASE64(`ClientId` + `:` + `ClientSecret`)</code>   

### Additional authorization method - Deribit signature credentials  The Derbit service provides dedicated authorization method, which harness user generated signature to increase security level for passing request data. Generated value is passed inside `Authorization` header, coded as:  <code>Authorization: deri-hmac-sha256 id=```ClientId```,ts=```Timestamp```,sig=```Signature```,nonce=```Nonce```</code>  where:  |Deribit credential|Description |- -- -|- -- -- -- -- --| |*ClientId*|Can be found on the API page on the Deribit website| |*Timestamp*|Time when the request was generated - given as **miliseconds**. It's valid for **60 seconds** since generation, after that time any request with an old timestamp will be rejected.| |*Signature*|Value for signature calculated as described below | |*Nonce*|Single usage, user generated initialization vector for the server token|  The signature is generated by the following formula:  <code> Signature = HEX_STRING( HMAC-SHA256( ClientSecret, StringToSign ) );</code></br>  <code> StringToSign =  Timestamp + \"\\n\" + Nonce + \"\\n\" + RequestData;</code></br>  <code> RequestData =  UPPERCASE(HTTP_METHOD())  + \"\\n\" + URI() + \"\\n\" + RequestBody + \"\\n\";</code></br>   e.g. (using shell with ```openssl``` tool):  <code>&nbsp;&nbsp;&nbsp;&nbsp;ClientId=AAAAAAAAAAA</code></br>  <code>&nbsp;&nbsp;&nbsp;&nbsp;ClientSecret=ABCD</code></br>  <code>&nbsp;&nbsp;&nbsp;&nbsp;Timestamp=$( date +%s000 )</code></br>  <code>&nbsp;&nbsp;&nbsp;&nbsp;Nonce=$( cat /dev/urandom | tr -dc 'a-z0-9' | head -c8 )</code></br>  <code>&nbsp;&nbsp;&nbsp;&nbsp;URI=\"/api/v2/private/get_account_summary?currency=BTC\"</code></br>  <code>&nbsp;&nbsp;&nbsp;&nbsp;HttpMethod=GET</code></br>  <code>&nbsp;&nbsp;&nbsp;&nbsp;Body=\"\"</code></br></br>  <code>&nbsp;&nbsp;&nbsp;&nbsp;Signature=$( echo -ne \"${Timestamp}\\n${Nonce}\\n${HttpMethod}\\n${URI}\\n${Body}\\n\" | openssl sha256 -r -hmac \"$ClientSecret\" | cut -f1 -d' ' )</code></br></br> <code>&nbsp;&nbsp;&nbsp;&nbsp;echo $Signature</code></br></br>  <code>&nbsp;&nbsp;&nbsp;&nbsp;shell output> ea40d5e5e4fae235ab22b61da98121fbf4acdc06db03d632e23c66bcccb90d2c  (**WARNING**: Exact value depends on current timestamp and client credentials</code></br></br>  <code>&nbsp;&nbsp;&nbsp;&nbsp;curl -s -X ${HttpMethod} -H \"Authorization: deri-hmac-sha256 id=${ClientId},ts=${Timestamp},nonce=${Nonce},sig=${Signature}\" \"https://www.deribit.com${URI}\"</code></br></br>    ### Additional authorization method - signature credentials (WebSocket API)  When connecting through Websocket, user can request for authorization using ```client_credential``` method, which requires providing following parameters (as a part of JSON request):  |JSON parameter|Description |- -- -|- -- -- -- -- --| |*grant_type*|Must be **client_signature**| |*client_id*|Can be found on the API page on the Deribit website| |*timestamp*|Time when the request was generated - given as **miliseconds**. It's valid for **60 seconds** since generation, after that time any request with an old timestamp will be rejected.| |*signature*|Value for signature calculated as described below | |*nonce*|Single usage, user generated initialization vector for the server token| |*data*|**Optional** field, which contains any user specific value|  The signature is generated by the following formula:  <code> StringToSign =  Timestamp + \"\\n\" + Nonce + \"\\n\" + Data;</code></br>  <code> Signature = HEX_STRING( HMAC-SHA256( ClientSecret, StringToSign ) );</code></br>   e.g. (using shell with ```openssl``` tool):  <code>&nbsp;&nbsp;&nbsp;&nbsp;ClientId=AAAAAAAAAAA</code></br>  <code>&nbsp;&nbsp;&nbsp;&nbsp;ClientSecret=ABCD</code></br>  <code>&nbsp;&nbsp;&nbsp;&nbsp;Timestamp=$( date +%s000 ) # e.g. 1554883365000 </code></br>  <code>&nbsp;&nbsp;&nbsp;&nbsp;Nonce=$( cat /dev/urandom | tr -dc 'a-z0-9' | head -c8 ) # e.g. fdbmmz79 </code></br>  <code>&nbsp;&nbsp;&nbsp;&nbsp;Data=\"\"</code></br></br>  <code>&nbsp;&nbsp;&nbsp;&nbsp;Signature=$( echo -ne \"${Timestamp}\\n${Nonce}\\n${Data}\\n\" | openssl sha256 -r -hmac \"$ClientSecret\" | cut -f1 -d' ' )</code></br></br> <code>&nbsp;&nbsp;&nbsp;&nbsp;echo $Signature</code></br></br>  <code>&nbsp;&nbsp;&nbsp;&nbsp;shell output> e20c9cd5639d41f8bbc88f4d699c4baf94a4f0ee320e9a116b72743c449eb994  (**WARNING**: Exact value depends on current timestamp and client credentials</code></br></br>   You can also check the signature value using some online tools like, e.g: [https://codebeautify.org/hmac-generator](https://codebeautify.org/hmac-generator) (but don't forget about adding *newline* after each part of the hashed text and remember that you **should use** it only with your **test credentials**).   Here's a sample JSON request created using the values from the example above:  <code> {                            </br> &nbsp;&nbsp;\"jsonrpc\" : \"2.0\",         </br> &nbsp;&nbsp;\"id\" : 9929,               </br> &nbsp;&nbsp;\"method\" : \"public/auth\",  </br> &nbsp;&nbsp;\"params\" :                 </br> &nbsp;&nbsp;{                        </br> &nbsp;&nbsp;&nbsp;&nbsp;\"grant_type\" : \"client_signature\",   </br> &nbsp;&nbsp;&nbsp;&nbsp;\"client_id\" : \"AAAAAAAAAAA\",         </br> &nbsp;&nbsp;&nbsp;&nbsp;\"timestamp\": \"1554883365000\",        </br> &nbsp;&nbsp;&nbsp;&nbsp;\"nonce\": \"fdbmmz79\",                 </br> &nbsp;&nbsp;&nbsp;&nbsp;\"data\": \"\",                          </br> &nbsp;&nbsp;&nbsp;&nbsp;\"signature\" : \"e20c9cd5639d41f8bbc88f4d699c4baf94a4f0ee320e9a116b72743c449eb994\"  </br> &nbsp;&nbsp;}                        </br> }                            </br> </code>   ### Access scope  When asking for `access token` user can provide the required access level (called `scope`) which defines what type of functionality he/she wants to use, and whether requests are only going to check for some data or also to update them.  Scopes are required and checked for `private` methods, so if you plan to use only `public` information you can stay with values assigned by default.  |Scope|Description |- -- -|- -- -- -- -- --| |*account:read*|Access to **account** methods - read only data| |*account:read_write*|Access to **account** methods - allows to manage account settings, add subaccounts, etc.| |*trade:read*|Access to **trade** methods - read only data| |*trade:read_write*|Access to **trade** methods - required to create and modify orders| |*wallet:read*|Access to **wallet** methods - read only data| |*wallet:read_write*|Access to **wallet** methods - allows to withdraw, generate new deposit address, etc.| |*wallet:none*, *account:none*, *trade:none*|Blocked access to specified functionality|    <span style=\"color:red\">**NOTICE:**</span> Depending on choosing an authentication method (```grant type```) some scopes could be narrowed by the server. e.g. when ```grant_type = client_credentials``` and ```scope = wallet:read_write``` it's modified by the server as ```scope = wallet:read```\"   ## JSON-RPC over websocket Websocket is the prefered transport mechanism for the JSON-RPC API, because it is faster and because it can support [subscriptions](#subscriptions) and [cancel on disconnect](#private-enable_cancel_on_disconnect). The code examples that can be found next to each of the methods show how websockets can be used from Python or Javascript/node.js.  ## JSON-RPC over HTTP Besides websockets it is also possible to use the API via HTTP. The code examples for 'shell' show how this can be done using curl. Note that subscriptions and cancel on disconnect are not supported via HTTP.  #Methods 

This C# SDK is automatically generated by the [OpenAPI Generator](https://openapi-generator.tech) project:

- API version: 2.0.0
- SDK version: 1.0.0
- Build package: org.openapitools.codegen.languages.CSharpClientCodegen

## Frameworks supported


- .NET 4.0 or later
- Windows Phone 7.1 (Mango)

## Dependencies


- [RestSharp](https://www.nuget.org/packages/RestSharp) - 105.1.0 or later
- [Json.NET](https://www.nuget.org/packages/Newtonsoft.Json/) - 7.0.0 or later
- [JsonSubTypes](https://www.nuget.org/packages/JsonSubTypes/) - 1.2.0 or later

The DLLs included in the package may not be the latest version. We recommend using [NuGet](https://docs.nuget.org/consume/installing-nuget) to obtain the latest version of the packages:

```
Install-Package RestSharp
Install-Package Newtonsoft.Json
Install-Package JsonSubTypes
```

NOTE: RestSharp versions greater than 105.1.0 have a bug which causes file uploads to fail. See [RestSharp#742](https://github.com/restsharp/RestSharp/issues/742)

## Installation

Run the following command to generate the DLL

- [Mac/Linux] `/bin/sh build.sh`
- [Windows] `build.bat`

Then include the DLL (under the `bin` folder) in the C# project, and use the namespaces:

```csharp
using Org.OpenAPITools.Api;
using Org.OpenAPITools.Client;
using Org.OpenAPITools.Model;

```


## Packaging

A `.nuspec` is included with the project. You can follow the Nuget quickstart to [create](https://docs.microsoft.com/en-us/nuget/quickstart/create-and-publish-a-package#create-the-package) and [publish](https://docs.microsoft.com/en-us/nuget/quickstart/create-and-publish-a-package#publish-the-package) packages.

This `.nuspec` uses placeholders from the `.csproj`, so build the `.csproj` directly:

```
nuget pack -Build -OutputDirectory out Org.OpenAPITools.csproj
```

Then, publish to a [local feed](https://docs.microsoft.com/en-us/nuget/hosting-packages/local-feeds) or [other host](https://docs.microsoft.com/en-us/nuget/hosting-packages/overview) and consume the new package via Nuget as usual.


## Getting Started

```csharp
using System.Diagnostics;
using Org.OpenAPITools.Api;
using Org.OpenAPITools.Client;
using Org.OpenAPITools.Model;

namespace Example
{
    public class Example
    {
        public static void Main()
        {

            Configuration.Default.BasePath = "https://www.deribit.com/api/v2";
            // Configure HTTP basic authorization: bearerAuth
            Configuration.Default.Username = "YOUR_USERNAME";
            Configuration.Default.Password = "YOUR_PASSWORD";

            var apiInstance = new AccountManagementApi(Configuration.Default);
            var sid = 56;  // int? | The user id for the subaccount
            var name = newUserName;  // string | The new user name

            try
            {
                // Change the user name for a subaccount
                Object result = apiInstance.PrivateChangeSubaccountNameGet(sid, name);
                Debug.WriteLine(result);
            }
            catch (ApiException e)
            {
                Debug.Print("Exception when calling AccountManagementApi.PrivateChangeSubaccountNameGet: " + e.Message );
                Debug.Print("Status Code: "+ e.ErrorCode);
                Debug.Print(e.StackTrace);
            }

        }
    }
}
```

## Documentation for API Endpoints

All URIs are relative to *https://www.deribit.com/api/v2*

Class | Method | HTTP request | Description
------------ | ------------- | ------------- | -------------
*AccountManagementApi* | [**PrivateChangeSubaccountNameGet**](docs/AccountManagementApi.md#privatechangesubaccountnameget) | **GET** /private/change_subaccount_name | Change the user name for a subaccount
*AccountManagementApi* | [**PrivateCreateSubaccountGet**](docs/AccountManagementApi.md#privatecreatesubaccountget) | **GET** /private/create_subaccount | Create a new subaccount
*AccountManagementApi* | [**PrivateDisableTfaForSubaccountGet**](docs/AccountManagementApi.md#privatedisabletfaforsubaccountget) | **GET** /private/disable_tfa_for_subaccount | Disable two factor authentication for a subaccount.
*AccountManagementApi* | [**PrivateGetAccountSummaryGet**](docs/AccountManagementApi.md#privategetaccountsummaryget) | **GET** /private/get_account_summary | Retrieves user account summary.
*AccountManagementApi* | [**PrivateGetEmailLanguageGet**](docs/AccountManagementApi.md#privategetemaillanguageget) | **GET** /private/get_email_language | Retrieves the language to be used for emails.
*AccountManagementApi* | [**PrivateGetNewAnnouncementsGet**](docs/AccountManagementApi.md#privategetnewannouncementsget) | **GET** /private/get_new_announcements | Retrieves announcements that have not been marked read by the user.
*AccountManagementApi* | [**PrivateGetPositionGet**](docs/AccountManagementApi.md#privategetpositionget) | **GET** /private/get_position | Retrieve user position.
*AccountManagementApi* | [**PrivateGetPositionsGet**](docs/AccountManagementApi.md#privategetpositionsget) | **GET** /private/get_positions | Retrieve user positions.
*AccountManagementApi* | [**PrivateGetSubaccountsGet**](docs/AccountManagementApi.md#privategetsubaccountsget) | **GET** /private/get_subaccounts | Get information about subaccounts
*AccountManagementApi* | [**PrivateSetAnnouncementAsReadGet**](docs/AccountManagementApi.md#privatesetannouncementasreadget) | **GET** /private/set_announcement_as_read | Marks an announcement as read, so it will not be shown in `get_new_announcements`.
*AccountManagementApi* | [**PrivateSetEmailForSubaccountGet**](docs/AccountManagementApi.md#privatesetemailforsubaccountget) | **GET** /private/set_email_for_subaccount | Assign an email address to a subaccount. User will receive an email with confirmation link.
*AccountManagementApi* | [**PrivateSetEmailLanguageGet**](docs/AccountManagementApi.md#privatesetemaillanguageget) | **GET** /private/set_email_language | Changes the language to be used for emails.
*AccountManagementApi* | [**PrivateSetPasswordForSubaccountGet**](docs/AccountManagementApi.md#privatesetpasswordforsubaccountget) | **GET** /private/set_password_for_subaccount | Set the password for the subaccount
*AccountManagementApi* | [**PrivateToggleNotificationsFromSubaccountGet**](docs/AccountManagementApi.md#privatetogglenotificationsfromsubaccountget) | **GET** /private/toggle_notifications_from_subaccount | Enable or disable sending of notifications for the subaccount.
*AccountManagementApi* | [**PrivateToggleSubaccountLoginGet**](docs/AccountManagementApi.md#privatetogglesubaccountloginget) | **GET** /private/toggle_subaccount_login | Enable or disable login for a subaccount. If login is disabled and a session for the subaccount exists, this session will be terminated.
*AccountManagementApi* | [**PublicGetAnnouncementsGet**](docs/AccountManagementApi.md#publicgetannouncementsget) | **GET** /public/get_announcements | Retrieves announcements from the last 30 days.
*AuthenticationApi* | [**PublicAuthGet**](docs/AuthenticationApi.md#publicauthget) | **GET** /public/auth | Authenticate
*InternalApi* | [**PrivateAddToAddressBookGet**](docs/InternalApi.md#privateaddtoaddressbookget) | **GET** /private/add_to_address_book | Adds new entry to address book of given type
*InternalApi* | [**PrivateDisableTfaWithRecoveryCodeGet**](docs/InternalApi.md#privatedisabletfawithrecoverycodeget) | **GET** /private/disable_tfa_with_recovery_code | Disables TFA with one time recovery code
*InternalApi* | [**PrivateGetAddressBookGet**](docs/InternalApi.md#privategetaddressbookget) | **GET** /private/get_address_book | Retrieves address book of given type
*InternalApi* | [**PrivateRemoveFromAddressBookGet**](docs/InternalApi.md#privateremovefromaddressbookget) | **GET** /private/remove_from_address_book | Adds new entry to address book of given type
*InternalApi* | [**PrivateSubmitTransferToSubaccountGet**](docs/InternalApi.md#privatesubmittransfertosubaccountget) | **GET** /private/submit_transfer_to_subaccount | Transfer funds to a subaccount.
*InternalApi* | [**PrivateSubmitTransferToUserGet**](docs/InternalApi.md#privatesubmittransfertouserget) | **GET** /private/submit_transfer_to_user | Transfer funds to a another user.
*InternalApi* | [**PrivateToggleDepositAddressCreationGet**](docs/InternalApi.md#privatetoggledepositaddresscreationget) | **GET** /private/toggle_deposit_address_creation | Enable or disable deposit address creation
*InternalApi* | [**PublicGetFooterGet**](docs/InternalApi.md#publicgetfooterget) | **GET** /public/get_footer | Get information to be displayed in the footer of the website.
*InternalApi* | [**PublicGetOptionMarkPricesGet**](docs/InternalApi.md#publicgetoptionmarkpricesget) | **GET** /public/get_option_mark_prices | Retrives market prices and its implied volatility of options instruments
*InternalApi* | [**PublicValidateFieldGet**](docs/InternalApi.md#publicvalidatefieldget) | **GET** /public/validate_field | Method used to introduce the client software connected to Deribit platform over websocket. Provided data may have an impact on the maintained connection and will be collected for internal statistical purposes. In response, Deribit will also introduce itself.
*MarketDataApi* | [**PublicGetBookSummaryByCurrencyGet**](docs/MarketDataApi.md#publicgetbooksummarybycurrencyget) | **GET** /public/get_book_summary_by_currency | Retrieves the summary information such as open interest, 24h volume, etc. for all instruments for the currency (optionally filtered by kind).
*MarketDataApi* | [**PublicGetBookSummaryByInstrumentGet**](docs/MarketDataApi.md#publicgetbooksummarybyinstrumentget) | **GET** /public/get_book_summary_by_instrument | Retrieves the summary information such as open interest, 24h volume, etc. for a specific instrument.
*MarketDataApi* | [**PublicGetContractSizeGet**](docs/MarketDataApi.md#publicgetcontractsizeget) | **GET** /public/get_contract_size | Retrieves contract size of provided instrument.
*MarketDataApi* | [**PublicGetCurrenciesGet**](docs/MarketDataApi.md#publicgetcurrenciesget) | **GET** /public/get_currencies | Retrieves all cryptocurrencies supported by the API.
*MarketDataApi* | [**PublicGetFundingChartDataGet**](docs/MarketDataApi.md#publicgetfundingchartdataget) | **GET** /public/get_funding_chart_data | Retrieve the latest user trades that have occurred for PERPETUAL instruments in a specific currency symbol and within given time range.
*MarketDataApi* | [**PublicGetHistoricalVolatilityGet**](docs/MarketDataApi.md#publicgethistoricalvolatilityget) | **GET** /public/get_historical_volatility | Provides information about historical volatility for given cryptocurrency.
*MarketDataApi* | [**PublicGetIndexGet**](docs/MarketDataApi.md#publicgetindexget) | **GET** /public/get_index | Retrieves the current index price for the instruments, for the selected currency.
*MarketDataApi* | [**PublicGetInstrumentsGet**](docs/MarketDataApi.md#publicgetinstrumentsget) | **GET** /public/get_instruments | Retrieves available trading instruments. This method can be used to see which instruments are available for trading, or which instruments have existed historically.
*MarketDataApi* | [**PublicGetLastSettlementsByCurrencyGet**](docs/MarketDataApi.md#publicgetlastsettlementsbycurrencyget) | **GET** /public/get_last_settlements_by_currency | Retrieves historical settlement, delivery and bankruptcy events coming from all instruments within given currency.
*MarketDataApi* | [**PublicGetLastSettlementsByInstrumentGet**](docs/MarketDataApi.md#publicgetlastsettlementsbyinstrumentget) | **GET** /public/get_last_settlements_by_instrument | Retrieves historical public settlement, delivery and bankruptcy events filtered by instrument name.
*MarketDataApi* | [**PublicGetLastTradesByCurrencyAndTimeGet**](docs/MarketDataApi.md#publicgetlasttradesbycurrencyandtimeget) | **GET** /public/get_last_trades_by_currency_and_time | Retrieve the latest trades that have occurred for instruments in a specific currency symbol and within given time range.
*MarketDataApi* | [**PublicGetLastTradesByCurrencyGet**](docs/MarketDataApi.md#publicgetlasttradesbycurrencyget) | **GET** /public/get_last_trades_by_currency | Retrieve the latest trades that have occurred for instruments in a specific currency symbol.
*MarketDataApi* | [**PublicGetLastTradesByInstrumentAndTimeGet**](docs/MarketDataApi.md#publicgetlasttradesbyinstrumentandtimeget) | **GET** /public/get_last_trades_by_instrument_and_time | Retrieve the latest trades that have occurred for a specific instrument and within given time range.
*MarketDataApi* | [**PublicGetLastTradesByInstrumentGet**](docs/MarketDataApi.md#publicgetlasttradesbyinstrumentget) | **GET** /public/get_last_trades_by_instrument | Retrieve the latest trades that have occurred for a specific instrument.
*MarketDataApi* | [**PublicGetOrderBookGet**](docs/MarketDataApi.md#publicgetorderbookget) | **GET** /public/get_order_book | Retrieves the order book, along with other market values for a given instrument.
*MarketDataApi* | [**PublicGetTradeVolumesGet**](docs/MarketDataApi.md#publicgettradevolumesget) | **GET** /public/get_trade_volumes | Retrieves aggregated 24h trade volumes for different instrument types and currencies.
*MarketDataApi* | [**PublicGetTradingviewChartDataGet**](docs/MarketDataApi.md#publicgettradingviewchartdataget) | **GET** /public/get_tradingview_chart_data | Publicly available market data used to generate a TradingView candle chart.
*MarketDataApi* | [**PublicTickerGet**](docs/MarketDataApi.md#publictickerget) | **GET** /public/ticker | Get ticker for an instrument.
*PrivateApi* | [**PrivateAddToAddressBookGet**](docs/PrivateApi.md#privateaddtoaddressbookget) | **GET** /private/add_to_address_book | Adds new entry to address book of given type
*PrivateApi* | [**PrivateBuyGet**](docs/PrivateApi.md#privatebuyget) | **GET** /private/buy | Places a buy order for an instrument.
*PrivateApi* | [**PrivateCancelAllByCurrencyGet**](docs/PrivateApi.md#privatecancelallbycurrencyget) | **GET** /private/cancel_all_by_currency | Cancels all orders by currency, optionally filtered by instrument kind and/or order type.
*PrivateApi* | [**PrivateCancelAllByInstrumentGet**](docs/PrivateApi.md#privatecancelallbyinstrumentget) | **GET** /private/cancel_all_by_instrument | Cancels all orders by instrument, optionally filtered by order type.
*PrivateApi* | [**PrivateCancelAllGet**](docs/PrivateApi.md#privatecancelallget) | **GET** /private/cancel_all | This method cancels all users orders and stop orders within all currencies and instrument kinds.
*PrivateApi* | [**PrivateCancelGet**](docs/PrivateApi.md#privatecancelget) | **GET** /private/cancel | Cancel an order, specified by order id
*PrivateApi* | [**PrivateCancelTransferByIdGet**](docs/PrivateApi.md#privatecanceltransferbyidget) | **GET** /private/cancel_transfer_by_id | Cancel transfer
*PrivateApi* | [**PrivateCancelWithdrawalGet**](docs/PrivateApi.md#privatecancelwithdrawalget) | **GET** /private/cancel_withdrawal | Cancels withdrawal request
*PrivateApi* | [**PrivateChangeSubaccountNameGet**](docs/PrivateApi.md#privatechangesubaccountnameget) | **GET** /private/change_subaccount_name | Change the user name for a subaccount
*PrivateApi* | [**PrivateClosePositionGet**](docs/PrivateApi.md#privateclosepositionget) | **GET** /private/close_position | Makes closing position reduce only order .
*PrivateApi* | [**PrivateCreateDepositAddressGet**](docs/PrivateApi.md#privatecreatedepositaddressget) | **GET** /private/create_deposit_address | Creates deposit address in currency
*PrivateApi* | [**PrivateCreateSubaccountGet**](docs/PrivateApi.md#privatecreatesubaccountget) | **GET** /private/create_subaccount | Create a new subaccount
*PrivateApi* | [**PrivateDisableTfaForSubaccountGet**](docs/PrivateApi.md#privatedisabletfaforsubaccountget) | **GET** /private/disable_tfa_for_subaccount | Disable two factor authentication for a subaccount.
*PrivateApi* | [**PrivateDisableTfaWithRecoveryCodeGet**](docs/PrivateApi.md#privatedisabletfawithrecoverycodeget) | **GET** /private/disable_tfa_with_recovery_code | Disables TFA with one time recovery code
*PrivateApi* | [**PrivateEditGet**](docs/PrivateApi.md#privateeditget) | **GET** /private/edit | Change price, amount and/or other properties of an order.
*PrivateApi* | [**PrivateGetAccountSummaryGet**](docs/PrivateApi.md#privategetaccountsummaryget) | **GET** /private/get_account_summary | Retrieves user account summary.
*PrivateApi* | [**PrivateGetAddressBookGet**](docs/PrivateApi.md#privategetaddressbookget) | **GET** /private/get_address_book | Retrieves address book of given type
*PrivateApi* | [**PrivateGetCurrentDepositAddressGet**](docs/PrivateApi.md#privategetcurrentdepositaddressget) | **GET** /private/get_current_deposit_address | Retrieve deposit address for currency
*PrivateApi* | [**PrivateGetDepositsGet**](docs/PrivateApi.md#privategetdepositsget) | **GET** /private/get_deposits | Retrieve the latest users deposits
*PrivateApi* | [**PrivateGetEmailLanguageGet**](docs/PrivateApi.md#privategetemaillanguageget) | **GET** /private/get_email_language | Retrieves the language to be used for emails.
*PrivateApi* | [**PrivateGetMarginsGet**](docs/PrivateApi.md#privategetmarginsget) | **GET** /private/get_margins | Get margins for given instrument, amount and price.
*PrivateApi* | [**PrivateGetNewAnnouncementsGet**](docs/PrivateApi.md#privategetnewannouncementsget) | **GET** /private/get_new_announcements | Retrieves announcements that have not been marked read by the user.
*PrivateApi* | [**PrivateGetOpenOrdersByCurrencyGet**](docs/PrivateApi.md#privategetopenordersbycurrencyget) | **GET** /private/get_open_orders_by_currency | Retrieves list of user's open orders.
*PrivateApi* | [**PrivateGetOpenOrdersByInstrumentGet**](docs/PrivateApi.md#privategetopenordersbyinstrumentget) | **GET** /private/get_open_orders_by_instrument | Retrieves list of user's open orders within given Instrument.
*PrivateApi* | [**PrivateGetOrderHistoryByCurrencyGet**](docs/PrivateApi.md#privategetorderhistorybycurrencyget) | **GET** /private/get_order_history_by_currency | Retrieves history of orders that have been partially or fully filled.
*PrivateApi* | [**PrivateGetOrderHistoryByInstrumentGet**](docs/PrivateApi.md#privategetorderhistorybyinstrumentget) | **GET** /private/get_order_history_by_instrument | Retrieves history of orders that have been partially or fully filled.
*PrivateApi* | [**PrivateGetOrderMarginByIdsGet**](docs/PrivateApi.md#privategetordermarginbyidsget) | **GET** /private/get_order_margin_by_ids | Retrieves initial margins of given orders
*PrivateApi* | [**PrivateGetOrderStateGet**](docs/PrivateApi.md#privategetorderstateget) | **GET** /private/get_order_state | Retrieve the current state of an order.
*PrivateApi* | [**PrivateGetPositionGet**](docs/PrivateApi.md#privategetpositionget) | **GET** /private/get_position | Retrieve user position.
*PrivateApi* | [**PrivateGetPositionsGet**](docs/PrivateApi.md#privategetpositionsget) | **GET** /private/get_positions | Retrieve user positions.
*PrivateApi* | [**PrivateGetSettlementHistoryByCurrencyGet**](docs/PrivateApi.md#privategetsettlementhistorybycurrencyget) | **GET** /private/get_settlement_history_by_currency | Retrieves settlement, delivery and bankruptcy events that have affected your account.
*PrivateApi* | [**PrivateGetSettlementHistoryByInstrumentGet**](docs/PrivateApi.md#privategetsettlementhistorybyinstrumentget) | **GET** /private/get_settlement_history_by_instrument | Retrieves public settlement, delivery and bankruptcy events filtered by instrument name
*PrivateApi* | [**PrivateGetSubaccountsGet**](docs/PrivateApi.md#privategetsubaccountsget) | **GET** /private/get_subaccounts | Get information about subaccounts
*PrivateApi* | [**PrivateGetTransfersGet**](docs/PrivateApi.md#privategettransfersget) | **GET** /private/get_transfers | Adds new entry to address book of given type
*PrivateApi* | [**PrivateGetUserTradesByCurrencyAndTimeGet**](docs/PrivateApi.md#privategetusertradesbycurrencyandtimeget) | **GET** /private/get_user_trades_by_currency_and_time | Retrieve the latest user trades that have occurred for instruments in a specific currency symbol and within given time range.
*PrivateApi* | [**PrivateGetUserTradesByCurrencyGet**](docs/PrivateApi.md#privategetusertradesbycurrencyget) | **GET** /private/get_user_trades_by_currency | Retrieve the latest user trades that have occurred for instruments in a specific currency symbol.
*PrivateApi* | [**PrivateGetUserTradesByInstrumentAndTimeGet**](docs/PrivateApi.md#privategetusertradesbyinstrumentandtimeget) | **GET** /private/get_user_trades_by_instrument_and_time | Retrieve the latest user trades that have occurred for a specific instrument and within given time range.
*PrivateApi* | [**PrivateGetUserTradesByInstrumentGet**](docs/PrivateApi.md#privategetusertradesbyinstrumentget) | **GET** /private/get_user_trades_by_instrument | Retrieve the latest user trades that have occurred for a specific instrument.
*PrivateApi* | [**PrivateGetUserTradesByOrderGet**](docs/PrivateApi.md#privategetusertradesbyorderget) | **GET** /private/get_user_trades_by_order | Retrieve the list of user trades that was created for given order
*PrivateApi* | [**PrivateGetWithdrawalsGet**](docs/PrivateApi.md#privategetwithdrawalsget) | **GET** /private/get_withdrawals | Retrieve the latest users withdrawals
*PrivateApi* | [**PrivateRemoveFromAddressBookGet**](docs/PrivateApi.md#privateremovefromaddressbookget) | **GET** /private/remove_from_address_book | Adds new entry to address book of given type
*PrivateApi* | [**PrivateSellGet**](docs/PrivateApi.md#privatesellget) | **GET** /private/sell | Places a sell order for an instrument.
*PrivateApi* | [**PrivateSetAnnouncementAsReadGet**](docs/PrivateApi.md#privatesetannouncementasreadget) | **GET** /private/set_announcement_as_read | Marks an announcement as read, so it will not be shown in `get_new_announcements`.
*PrivateApi* | [**PrivateSetEmailForSubaccountGet**](docs/PrivateApi.md#privatesetemailforsubaccountget) | **GET** /private/set_email_for_subaccount | Assign an email address to a subaccount. User will receive an email with confirmation link.
*PrivateApi* | [**PrivateSetEmailLanguageGet**](docs/PrivateApi.md#privatesetemaillanguageget) | **GET** /private/set_email_language | Changes the language to be used for emails.
*PrivateApi* | [**PrivateSetPasswordForSubaccountGet**](docs/PrivateApi.md#privatesetpasswordforsubaccountget) | **GET** /private/set_password_for_subaccount | Set the password for the subaccount
*PrivateApi* | [**PrivateSubmitTransferToSubaccountGet**](docs/PrivateApi.md#privatesubmittransfertosubaccountget) | **GET** /private/submit_transfer_to_subaccount | Transfer funds to a subaccount.
*PrivateApi* | [**PrivateSubmitTransferToUserGet**](docs/PrivateApi.md#privatesubmittransfertouserget) | **GET** /private/submit_transfer_to_user | Transfer funds to a another user.
*PrivateApi* | [**PrivateToggleDepositAddressCreationGet**](docs/PrivateApi.md#privatetoggledepositaddresscreationget) | **GET** /private/toggle_deposit_address_creation | Enable or disable deposit address creation
*PrivateApi* | [**PrivateToggleNotificationsFromSubaccountGet**](docs/PrivateApi.md#privatetogglenotificationsfromsubaccountget) | **GET** /private/toggle_notifications_from_subaccount | Enable or disable sending of notifications for the subaccount.
*PrivateApi* | [**PrivateToggleSubaccountLoginGet**](docs/PrivateApi.md#privatetogglesubaccountloginget) | **GET** /private/toggle_subaccount_login | Enable or disable login for a subaccount. If login is disabled and a session for the subaccount exists, this session will be terminated.
*PrivateApi* | [**PrivateWithdrawGet**](docs/PrivateApi.md#privatewithdrawget) | **GET** /private/withdraw | Creates a new withdrawal request
*PublicApi* | [**PublicAuthGet**](docs/PublicApi.md#publicauthget) | **GET** /public/auth | Authenticate
*PublicApi* | [**PublicGetAnnouncementsGet**](docs/PublicApi.md#publicgetannouncementsget) | **GET** /public/get_announcements | Retrieves announcements from the last 30 days.
*PublicApi* | [**PublicGetBookSummaryByCurrencyGet**](docs/PublicApi.md#publicgetbooksummarybycurrencyget) | **GET** /public/get_book_summary_by_currency | Retrieves the summary information such as open interest, 24h volume, etc. for all instruments for the currency (optionally filtered by kind).
*PublicApi* | [**PublicGetBookSummaryByInstrumentGet**](docs/PublicApi.md#publicgetbooksummarybyinstrumentget) | **GET** /public/get_book_summary_by_instrument | Retrieves the summary information such as open interest, 24h volume, etc. for a specific instrument.
*PublicApi* | [**PublicGetContractSizeGet**](docs/PublicApi.md#publicgetcontractsizeget) | **GET** /public/get_contract_size | Retrieves contract size of provided instrument.
*PublicApi* | [**PublicGetCurrenciesGet**](docs/PublicApi.md#publicgetcurrenciesget) | **GET** /public/get_currencies | Retrieves all cryptocurrencies supported by the API.
*PublicApi* | [**PublicGetFundingChartDataGet**](docs/PublicApi.md#publicgetfundingchartdataget) | **GET** /public/get_funding_chart_data | Retrieve the latest user trades that have occurred for PERPETUAL instruments in a specific currency symbol and within given time range.
*PublicApi* | [**PublicGetHistoricalVolatilityGet**](docs/PublicApi.md#publicgethistoricalvolatilityget) | **GET** /public/get_historical_volatility | Provides information about historical volatility for given cryptocurrency.
*PublicApi* | [**PublicGetIndexGet**](docs/PublicApi.md#publicgetindexget) | **GET** /public/get_index | Retrieves the current index price for the instruments, for the selected currency.
*PublicApi* | [**PublicGetInstrumentsGet**](docs/PublicApi.md#publicgetinstrumentsget) | **GET** /public/get_instruments | Retrieves available trading instruments. This method can be used to see which instruments are available for trading, or which instruments have existed historically.
*PublicApi* | [**PublicGetLastSettlementsByCurrencyGet**](docs/PublicApi.md#publicgetlastsettlementsbycurrencyget) | **GET** /public/get_last_settlements_by_currency | Retrieves historical settlement, delivery and bankruptcy events coming from all instruments within given currency.
*PublicApi* | [**PublicGetLastSettlementsByInstrumentGet**](docs/PublicApi.md#publicgetlastsettlementsbyinstrumentget) | **GET** /public/get_last_settlements_by_instrument | Retrieves historical public settlement, delivery and bankruptcy events filtered by instrument name.
*PublicApi* | [**PublicGetLastTradesByCurrencyAndTimeGet**](docs/PublicApi.md#publicgetlasttradesbycurrencyandtimeget) | **GET** /public/get_last_trades_by_currency_and_time | Retrieve the latest trades that have occurred for instruments in a specific currency symbol and within given time range.
*PublicApi* | [**PublicGetLastTradesByCurrencyGet**](docs/PublicApi.md#publicgetlasttradesbycurrencyget) | **GET** /public/get_last_trades_by_currency | Retrieve the latest trades that have occurred for instruments in a specific currency symbol.
*PublicApi* | [**PublicGetLastTradesByInstrumentAndTimeGet**](docs/PublicApi.md#publicgetlasttradesbyinstrumentandtimeget) | **GET** /public/get_last_trades_by_instrument_and_time | Retrieve the latest trades that have occurred for a specific instrument and within given time range.
*PublicApi* | [**PublicGetLastTradesByInstrumentGet**](docs/PublicApi.md#publicgetlasttradesbyinstrumentget) | **GET** /public/get_last_trades_by_instrument | Retrieve the latest trades that have occurred for a specific instrument.
*PublicApi* | [**PublicGetOrderBookGet**](docs/PublicApi.md#publicgetorderbookget) | **GET** /public/get_order_book | Retrieves the order book, along with other market values for a given instrument.
*PublicApi* | [**PublicGetTimeGet**](docs/PublicApi.md#publicgettimeget) | **GET** /public/get_time | Retrieves the current time (in milliseconds). This API endpoint can be used to check the clock skew between your software and Deribit's systems.
*PublicApi* | [**PublicGetTradeVolumesGet**](docs/PublicApi.md#publicgettradevolumesget) | **GET** /public/get_trade_volumes | Retrieves aggregated 24h trade volumes for different instrument types and currencies.
*PublicApi* | [**PublicGetTradingviewChartDataGet**](docs/PublicApi.md#publicgettradingviewchartdataget) | **GET** /public/get_tradingview_chart_data | Publicly available market data used to generate a TradingView candle chart.
*PublicApi* | [**PublicTestGet**](docs/PublicApi.md#publictestget) | **GET** /public/test | Tests the connection to the API server, and returns its version. You can use this to make sure the API is reachable, and matches the expected version.
*PublicApi* | [**PublicTickerGet**](docs/PublicApi.md#publictickerget) | **GET** /public/ticker | Get ticker for an instrument.
*PublicApi* | [**PublicValidateFieldGet**](docs/PublicApi.md#publicvalidatefieldget) | **GET** /public/validate_field | Method used to introduce the client software connected to Deribit platform over websocket. Provided data may have an impact on the maintained connection and will be collected for internal statistical purposes. In response, Deribit will also introduce itself.
*SupportingApi* | [**PublicGetTimeGet**](docs/SupportingApi.md#publicgettimeget) | **GET** /public/get_time | Retrieves the current time (in milliseconds). This API endpoint can be used to check the clock skew between your software and Deribit's systems.
*SupportingApi* | [**PublicTestGet**](docs/SupportingApi.md#publictestget) | **GET** /public/test | Tests the connection to the API server, and returns its version. You can use this to make sure the API is reachable, and matches the expected version.
*TradingApi* | [**PrivateBuyGet**](docs/TradingApi.md#privatebuyget) | **GET** /private/buy | Places a buy order for an instrument.
*TradingApi* | [**PrivateCancelAllByCurrencyGet**](docs/TradingApi.md#privatecancelallbycurrencyget) | **GET** /private/cancel_all_by_currency | Cancels all orders by currency, optionally filtered by instrument kind and/or order type.
*TradingApi* | [**PrivateCancelAllByInstrumentGet**](docs/TradingApi.md#privatecancelallbyinstrumentget) | **GET** /private/cancel_all_by_instrument | Cancels all orders by instrument, optionally filtered by order type.
*TradingApi* | [**PrivateCancelAllGet**](docs/TradingApi.md#privatecancelallget) | **GET** /private/cancel_all | This method cancels all users orders and stop orders within all currencies and instrument kinds.
*TradingApi* | [**PrivateCancelGet**](docs/TradingApi.md#privatecancelget) | **GET** /private/cancel | Cancel an order, specified by order id
*TradingApi* | [**PrivateClosePositionGet**](docs/TradingApi.md#privateclosepositionget) | **GET** /private/close_position | Makes closing position reduce only order .
*TradingApi* | [**PrivateEditGet**](docs/TradingApi.md#privateeditget) | **GET** /private/edit | Change price, amount and/or other properties of an order.
*TradingApi* | [**PrivateGetMarginsGet**](docs/TradingApi.md#privategetmarginsget) | **GET** /private/get_margins | Get margins for given instrument, amount and price.
*TradingApi* | [**PrivateGetOpenOrdersByCurrencyGet**](docs/TradingApi.md#privategetopenordersbycurrencyget) | **GET** /private/get_open_orders_by_currency | Retrieves list of user's open orders.
*TradingApi* | [**PrivateGetOpenOrdersByInstrumentGet**](docs/TradingApi.md#privategetopenordersbyinstrumentget) | **GET** /private/get_open_orders_by_instrument | Retrieves list of user's open orders within given Instrument.
*TradingApi* | [**PrivateGetOrderHistoryByCurrencyGet**](docs/TradingApi.md#privategetorderhistorybycurrencyget) | **GET** /private/get_order_history_by_currency | Retrieves history of orders that have been partially or fully filled.
*TradingApi* | [**PrivateGetOrderHistoryByInstrumentGet**](docs/TradingApi.md#privategetorderhistorybyinstrumentget) | **GET** /private/get_order_history_by_instrument | Retrieves history of orders that have been partially or fully filled.
*TradingApi* | [**PrivateGetOrderMarginByIdsGet**](docs/TradingApi.md#privategetordermarginbyidsget) | **GET** /private/get_order_margin_by_ids | Retrieves initial margins of given orders
*TradingApi* | [**PrivateGetOrderStateGet**](docs/TradingApi.md#privategetorderstateget) | **GET** /private/get_order_state | Retrieve the current state of an order.
*TradingApi* | [**PrivateGetSettlementHistoryByCurrencyGet**](docs/TradingApi.md#privategetsettlementhistorybycurrencyget) | **GET** /private/get_settlement_history_by_currency | Retrieves settlement, delivery and bankruptcy events that have affected your account.
*TradingApi* | [**PrivateGetSettlementHistoryByInstrumentGet**](docs/TradingApi.md#privategetsettlementhistorybyinstrumentget) | **GET** /private/get_settlement_history_by_instrument | Retrieves public settlement, delivery and bankruptcy events filtered by instrument name
*TradingApi* | [**PrivateGetUserTradesByCurrencyAndTimeGet**](docs/TradingApi.md#privategetusertradesbycurrencyandtimeget) | **GET** /private/get_user_trades_by_currency_and_time | Retrieve the latest user trades that have occurred for instruments in a specific currency symbol and within given time range.
*TradingApi* | [**PrivateGetUserTradesByCurrencyGet**](docs/TradingApi.md#privategetusertradesbycurrencyget) | **GET** /private/get_user_trades_by_currency | Retrieve the latest user trades that have occurred for instruments in a specific currency symbol.
*TradingApi* | [**PrivateGetUserTradesByInstrumentAndTimeGet**](docs/TradingApi.md#privategetusertradesbyinstrumentandtimeget) | **GET** /private/get_user_trades_by_instrument_and_time | Retrieve the latest user trades that have occurred for a specific instrument and within given time range.
*TradingApi* | [**PrivateGetUserTradesByInstrumentGet**](docs/TradingApi.md#privategetusertradesbyinstrumentget) | **GET** /private/get_user_trades_by_instrument | Retrieve the latest user trades that have occurred for a specific instrument.
*TradingApi* | [**PrivateGetUserTradesByOrderGet**](docs/TradingApi.md#privategetusertradesbyorderget) | **GET** /private/get_user_trades_by_order | Retrieve the list of user trades that was created for given order
*TradingApi* | [**PrivateSellGet**](docs/TradingApi.md#privatesellget) | **GET** /private/sell | Places a sell order for an instrument.
*WalletApi* | [**PrivateAddToAddressBookGet**](docs/WalletApi.md#privateaddtoaddressbookget) | **GET** /private/add_to_address_book | Adds new entry to address book of given type
*WalletApi* | [**PrivateCancelTransferByIdGet**](docs/WalletApi.md#privatecanceltransferbyidget) | **GET** /private/cancel_transfer_by_id | Cancel transfer
*WalletApi* | [**PrivateCancelWithdrawalGet**](docs/WalletApi.md#privatecancelwithdrawalget) | **GET** /private/cancel_withdrawal | Cancels withdrawal request
*WalletApi* | [**PrivateCreateDepositAddressGet**](docs/WalletApi.md#privatecreatedepositaddressget) | **GET** /private/create_deposit_address | Creates deposit address in currency
*WalletApi* | [**PrivateGetAddressBookGet**](docs/WalletApi.md#privategetaddressbookget) | **GET** /private/get_address_book | Retrieves address book of given type
*WalletApi* | [**PrivateGetCurrentDepositAddressGet**](docs/WalletApi.md#privategetcurrentdepositaddressget) | **GET** /private/get_current_deposit_address | Retrieve deposit address for currency
*WalletApi* | [**PrivateGetDepositsGet**](docs/WalletApi.md#privategetdepositsget) | **GET** /private/get_deposits | Retrieve the latest users deposits
*WalletApi* | [**PrivateGetTransfersGet**](docs/WalletApi.md#privategettransfersget) | **GET** /private/get_transfers | Adds new entry to address book of given type
*WalletApi* | [**PrivateGetWithdrawalsGet**](docs/WalletApi.md#privategetwithdrawalsget) | **GET** /private/get_withdrawals | Retrieve the latest users withdrawals
*WalletApi* | [**PrivateRemoveFromAddressBookGet**](docs/WalletApi.md#privateremovefromaddressbookget) | **GET** /private/remove_from_address_book | Adds new entry to address book of given type
*WalletApi* | [**PrivateSubmitTransferToSubaccountGet**](docs/WalletApi.md#privatesubmittransfertosubaccountget) | **GET** /private/submit_transfer_to_subaccount | Transfer funds to a subaccount.
*WalletApi* | [**PrivateSubmitTransferToUserGet**](docs/WalletApi.md#privatesubmittransfertouserget) | **GET** /private/submit_transfer_to_user | Transfer funds to a another user.
*WalletApi* | [**PrivateToggleDepositAddressCreationGet**](docs/WalletApi.md#privatetoggledepositaddresscreationget) | **GET** /private/toggle_deposit_address_creation | Enable or disable deposit address creation
*WalletApi* | [**PrivateWithdrawGet**](docs/WalletApi.md#privatewithdrawget) | **GET** /private/withdraw | Creates a new withdrawal request


## Documentation for Models

 - [Model.AddressBookItem](docs/AddressBookItem.md)
 - [Model.BookSummary](docs/BookSummary.md)
 - [Model.Currency](docs/Currency.md)
 - [Model.CurrencyPortfolio](docs/CurrencyPortfolio.md)
 - [Model.CurrencyWithdrawalPriorities](docs/CurrencyWithdrawalPriorities.md)
 - [Model.Deposit](docs/Deposit.md)
 - [Model.Instrument](docs/Instrument.md)
 - [Model.KeyNumberPair](docs/KeyNumberPair.md)
 - [Model.Order](docs/Order.md)
 - [Model.OrderIdInitialMarginPair](docs/OrderIdInitialMarginPair.md)
 - [Model.Portfolio](docs/Portfolio.md)
 - [Model.PortfolioEth](docs/PortfolioEth.md)
 - [Model.Position](docs/Position.md)
 - [Model.PublicTrade](docs/PublicTrade.md)
 - [Model.Settlement](docs/Settlement.md)
 - [Model.TradesVolumes](docs/TradesVolumes.md)
 - [Model.TransferItem](docs/TransferItem.md)
 - [Model.Types](docs/Types.md)
 - [Model.UserTrade](docs/UserTrade.md)
 - [Model.Withdrawal](docs/Withdrawal.md)


## Documentation for Authorization


### bearerAuth


- **Type**: HTTP basic authentication

