# openapi-client
# Overview  
Deribit provides three different interfaces to access the API:
* [JSON-RPC over Websocket](#json-rpc) 
* [JSON-RPC over HTTP](#json-rpc) 
* [FIX](#fix-api) (Financial Information eXchange)  
With the API Console you can use and test the JSON-RPC API, both via HTTP and  via Websocket. To visit the API console, go to __Account > API tab >  API Console tab.__   
## Naming 
Deribit tradeable assets or instruments use the following system of naming:  
|Kind|Examples|Template|Comments| |----|--------|--------|--------| 
|Future|<code>BTC-25MAR16</code>, <code>BTC-5AUG16</code>|<code>BTC-DMMMYY</code>|<code>BTC</code> is currency, <code>DMMMYY</code> is expiration date, <code>D</code> stands for day of month (1 or 2 digits), <code>MMM</code> - month (3 first letters in English), <code>YY</code> stands for year.| |Perpetual|<code>BTC-PERPETUAL</code>                        ||Perpetual contract for currency <code>BTC</code>.| |Option|<code>BTC-25MAR16-420-C</code>, <code>BTC-5AUG16-580-P</code>|<code>BTC-DMMMYY-STRIKE-K</code>|<code>STRIKE</code> is option strike price in USD. Template <code>K</code> is option kind: <code>C</code> for call options or <code>P</code> for put options.|   # JSON-RPC JSON-RPC is a light-weight remote procedure call (RPC) protocol. The  [JSON-RPC specification](https://www.jsonrpc.org/specification) defines the data structures that are used for the messages that are exchanged between client and server, as well as the rules around their processing. JSON-RPC uses JSON (RFC 4627) as data format.  JSON-RPC is transport agnostic: it does not specify which transport mechanism must be used. The Deribit API supports both Websocket (preferred) and HTTP (with limitations: subscriptions are not supported over HTTP).  
## Request messages 
> An example of a request message:  ```json {     \"jsonrpc\": \"2.0\",     \"id\": 8066,     \"method\": \"public/ticker\",     \"params\": {         \"instrument\": \"BTC-24AUG18-6500-P\"     } } ```  According to the JSON-RPC sepcification the requests must be JSON objects with the following fields.  |Name|Type|Description| |----|----|-----------| |jsonrpc|string|The version of the JSON-RPC spec: \"2.0\"| |id|integer or string|An identifier of the request. If it is included, then the response will contain the same identifier| |method|string|The method to be invoked| |params|object|The parameters values for the method. The field names must match with the expected parameter names. The parameters that are expected are described in the documentation for the methods, below.|  <aside class=\"warning\"> The JSON-RPC specification describes two features that are currently not supported by the API:  <ul> <li>Specification of parameter values by position</li> <li>Batch requests</li> </ul>  </aside>   
# Response messages 
> An example of a response message:  
```json {     \"jsonrpc\": \"2.0\",     \"id\": 5239,     \"testnet\": false,     \"result\": [         {             \"currency\": \"BTC\",             \"currencyLong\": \"Bitcoin\",             \"minConfirmation\": 2,             \"txFee\": 0.0006,             \"isActive\": true,             \"coinType\": \"BITCOIN\",             \"baseAddress\": null         }     ],     \"usIn\": 1535043730126248,     \"usOut\": 1535043730126250,     \"usDiff\": 2 } ```  
The JSON-RPC API always responds with a JSON object with the following fields.   |Name|Type|Description| |----|----|-----------| |id|integer|This is the same id that was sent in the request.| |result|any|If successful, the result of the API call. The format for the result is described with each method.| |error|error object|Only present if there was an error invoking the method. The error object is described below.| |testnet|boolean|Indicates whether the API in use is actually the test API.  <code>false</code> for production server, <code>true</code> for test server.| |usIn|integer|The timestamp when the requests was received (microseconds since the Unix epoch)| |usOut|integer|The timestamp when the response was sent (microseconds since the Unix epoch)| |usDiff|integer|The number of microseconds that was spent handling the request|  <aside class=\"notice\"> The fields <code>testnet</code>, <code>usIn</code>, <code>usOut</code> and <code>usDiff</code> are not part of the JSON-RPC standard.  <p>In order not to clutter the examples they will generally be omitted from the example code.</p> </aside>  > An example of a response with an error:  ```json {     \"jsonrpc\": \"2.0\",     \"id\": 8163,     \"error\": {         \"code\": 11050,         \"message\": \"bad_request\"     },     \"testnet\": false,     \"usIn\": 1535037392434763,     \"usOut\": 1535037392448119,     \"usDiff\": 13356 } ``` In case of an error the response message will contain the error field, with as value an object with the following with the following fields:  |Name|Type|Description |----|----|-----------| |code|integer|A number that indicates the kind of error.| |message|string|A short description that indicates the kind of error.| |data|any|Additional data about the error. This field may be omitted.|  
## Notifications  
> An example of a notification:  
```json {     \"jsonrpc\": \"2.0\",     \"method\": \"subscription\",     \"params\": {         \"channel\": \"deribit_price_index.btc_usd\",         \"data\": {             \"timestamp\": 1535098298227,             \"price\": 6521.17,             \"index_name\": \"btc_usd\"         }     } } ```  
API users can subscribe to certain types of notifications. This means that they will receive JSON-RPC notification-messages from the server when certain events occur, such as changes to the index price or changes to the order book for a certain instrument.   
The API methods [public/subscribe](#public-subscribe) and [private/subscribe](#private-subscribe) are used to set up a subscription. Since HTTP does not support the sending of messages from server to client, these methods are only availble when using the Websocket transport mechanism.  At the moment of subscription a \"channel\" must be specified. The channel determines the type of events that will be received.  See [Subscriptions](#subscriptions) for more details about the channels.  In accordance with the JSON-RPC specification, the format of a notification  is that of a request message without an <code>id</code> field. The value of the <code>method</code> field will always be <code>\"subscription\"</code>. The <code>params</code> field will always be an object with 2 members: <code>channel</code> and <code>data</code>. The value of the <code>channel</code> member is the name of the channel (a string). The value of the <code>data</code> member is an object that contains data  that is specific for the channel.   
## Authentication  
> An example of a JSON request with token:  
```json {     \"id\": 5647,     \"method\": \"private/get_subaccounts\",     \"params\": {         \"access_token\": \"67SVutDoVZSzkUStHSuk51WntMNBJ5mh5DYZhwzpiqDF\"     } } ```  
The API consists of `public` and `private` methods. The public methods do not require authentication. The private methods use OAuth 2.0 authentication. This means that a valid OAuth access token must be included in the request, which can get achived by calling method [public/auth](#public-auth).  When the token was assigned to the user, it should be passed along, with other request parameters, back to the server:  
|Connection type|Access token placement |----|-----------| |**Websocket**|Inside request JSON parameters, as an `access_token` field| |**HTTP (REST)**|Header `Authorization: bearer ```Token``` ` value|  
### Additional authorization method - basic user credentials  
<span style=\"color:red\"><b> ! Not recommended - however, it could be useful for quick testing API</b></span></br>  Every `private` method could be accessed by providing, inside HTTP `Authorization: Basic XXX` header, values with user `ClientId` and assigned `ClientSecret` (both values can be found on the API page on the Deribit website) encoded with `Base64`:  <code>Authorization: Basic BASE64(`ClientId` + `:` + `ClientSecret`)</code>   
### Additional authorization method - Deribit signature credentials  
The Derbit service provides dedicated authorization method, which harness user generated signature to increase security level for passing request data. Generated value is passed inside `Authorization` header, coded as:  <code>Authorization: deri-hmac-sha256 id=```ClientId```,ts=```Timestamp```,sig=```Signature```,nonce=```Nonce```</code>  where:  |Deribit credential|Description |----|-----------| |*ClientId*|Can be found on the API page on the Deribit website| |*Timestamp*|Time when the request was generated - given as **miliseconds**. It's valid for **60 seconds** since generation, after that time any request with an old timestamp will be rejected.| |*Signature*|Value for signature calculated as described below | |*Nonce*|Single usage, user generated initialization vector for the server token|  The signature is generated by the following formula:  <code> Signature = HEX_STRING( HMAC-SHA256( ClientSecret, StringToSign ) );</code></br>  <code> StringToSign =  Timestamp + \"\\n\" + Nonce + \"\\n\" + RequestData;</code></br>  <code> RequestData =  UPPERCASE(HTTP_METHOD())  + \"\\n\" + URI() + \"\\n\" + RequestBody + \"\\n\";</code></br>   e.g. (using shell with ```openssl``` tool):  <code>&nbsp;&nbsp;&nbsp;&nbsp;ClientId=AAAAAAAAAAA</code></br>  <code>&nbsp;&nbsp;&nbsp;&nbsp;ClientSecret=ABCD</code></br>  <code>&nbsp;&nbsp;&nbsp;&nbsp;Timestamp=$( date +%s000 )</code></br>  <code>&nbsp;&nbsp;&nbsp;&nbsp;Nonce=$( cat /dev/urandom | tr -dc 'a-z0-9' | head -c8 )</code></br>  <code>&nbsp;&nbsp;&nbsp;&nbsp;URI=\"/api/v2/private/get_account_summary?currency=BTC\"</code></br>  <code>&nbsp;&nbsp;&nbsp;&nbsp;HttpMethod=GET</code></br>  <code>&nbsp;&nbsp;&nbsp;&nbsp;Body=\"\"</code></br></br>  <code>&nbsp;&nbsp;&nbsp;&nbsp;Signature=$( echo -ne \"${Timestamp}\\n${Nonce}\\n${HttpMethod}\\n${URI}\\n${Body}\\n\" | openssl sha256 -r -hmac \"$ClientSecret\" | cut -f1 -d' ' )</code></br></br> <code>&nbsp;&nbsp;&nbsp;&nbsp;echo $Signature</code></br></br>  <code>&nbsp;&nbsp;&nbsp;&nbsp;shell output> ea40d5e5e4fae235ab22b61da98121fbf4acdc06db03d632e23c66bcccb90d2c  (**WARNING**: Exact value depends on current timestamp and client credentials</code></br></br>  <code>&nbsp;&nbsp;&nbsp;&nbsp;curl -s -X ${HttpMethod} -H \"Authorization: deri-hmac-sha256 id=${ClientId},ts=${Timestamp},nonce=${Nonce},sig=${Signature}\" \"https://www.deribit.com${URI}\"</code></br></br>    
### Additional authorization method - signature credentials (WebSocket API)  
When connecting through Websocket, user can request for authorization using ```client_credential``` method, which requires providing following parameters (as a part of JSON request):  
|JSON parameter|Description |----|-----------| |*grant_type*|Must be **client_signature**| |*client_id*|Can be found on the API page on the Deribit website| |*timestamp*|Time when the request was generated - given as **miliseconds**. It's valid for **60 seconds** since generation, after that time any request with an old timestamp will be rejected.| |*signature*|Value for signature calculated as described below | |*nonce*|Single usage, user generated initialization vector for the server token| |*data*|**Optional** field, which contains any user specific value|  
The signature is generated by the following formula:  <code> StringToSign =  Timestamp + \"\\n\" + Nonce + \"\\n\" + Data;</code></br>  <code> Signature = HEX_STRING( HMAC-SHA256( ClientSecret, StringToSign ) );</code></br>   e.g. (using shell with ```openssl``` tool):  <code>&nbsp;&nbsp;&nbsp;&nbsp;ClientId=AAAAAAAAAAA</code></br>  <code>&nbsp;&nbsp;&nbsp;&nbsp;ClientSecret=ABCD</code></br>  <code>&nbsp;&nbsp;&nbsp;&nbsp;Timestamp=$( date +%s000 ) # e.g. 1554883365000 </code></br>  <code>&nbsp;&nbsp;&nbsp;&nbsp;Nonce=$( cat /dev/urandom | tr -dc 'a-z0-9' | head -c8 ) # e.g. fdbmmz79 </code></br>  <code>&nbsp;&nbsp;&nbsp;&nbsp;Data=\"\"</code></br></br>  <code>&nbsp;&nbsp;&nbsp;&nbsp;Signature=$( echo -ne \"${Timestamp}\\n${Nonce}\\n${Data}\\n\" | openssl sha256 -r -hmac \"$ClientSecret\" | cut -f1 -d' ' )</code></br></br> <code>&nbsp;&nbsp;&nbsp;&nbsp;echo $Signature</code></br></br>  <code>&nbsp;&nbsp;&nbsp;&nbsp;shell output> e20c9cd5639d41f8bbc88f4d699c4baf94a4f0ee320e9a116b72743c449eb994  (**WARNING**: Exact value depends on current timestamp and client credentials</code></br></br>   
You can also check the signature value using some online tools like, e.g: [https://codebeautify.org/hmac-generator](https://codebeautify.org/hmac-generator) (but don't forget about adding *newline* after each part of the hashed text and remember that you **should use** it only with your **test credentials**).   Here's a sample JSON request created using the values from the example above:  <code> {                            </br> &nbsp;&nbsp;\"jsonrpc\" : \"2.0\",         </br> &nbsp;&nbsp;\"id\" : 9929,               </br> &nbsp;&nbsp;\"method\" : \"public/auth\",  </br> &nbsp;&nbsp;\"params\" :                 </br> &nbsp;&nbsp;{                        </br> &nbsp;&nbsp;&nbsp;&nbsp;\"grant_type\" : \"client_signature\",   </br> &nbsp;&nbsp;&nbsp;&nbsp;\"client_id\" : \"AAAAAAAAAAA\",         </br> &nbsp;&nbsp;&nbsp;&nbsp;\"timestamp\": \"1554883365000\",        </br> &nbsp;&nbsp;&nbsp;&nbsp;\"nonce\": \"fdbmmz79\",                 </br> &nbsp;&nbsp;&nbsp;&nbsp;\"data\": \"\",                          </br> &nbsp;&nbsp;&nbsp;&nbsp;\"signature\" : \"e20c9cd5639d41f8bbc88f4d699c4baf94a4f0ee320e9a116b72743c449eb994\"  </br> &nbsp;&nbsp;}                        </br> }                            </br> </code>   
### Access scope  
When asking for `access token` user can provide the required access level (called `scope`) which defines what type of functionality he/she wants to use, and whether requests are only going to check for some data or also to update them.  Scopes are required and checked for `private` methods, so if you plan to use only `public` information you can stay with values assigned by default.  
|Scope|Description |----|-----------| |*account:read*|Access to **account** methods - read only data| |*account:read_write*|Access to **account** methods - allows to manage account settings, add subaccounts, etc.| |*trade:read*|Access to **trade** methods - read only data| |*trade:read_write*|Access to **trade** methods - required to create and modify orders| |*wallet:read*|Access to **wallet** methods - read only data| |*wallet:read_write*|Access to **wallet** methods - allows to withdraw, generate new deposit address, etc.| |*wallet:none*, *account:none*, *trade:none*|Blocked access to specified functionality|    <span style=\"color:red\">**NOTICE:**</span> Depending on choosing an authentication method (```grant type```) some scopes could be narrowed by the server. e.g. when ```grant_type = client_credentials``` and ```scope = wallet:read_write``` it's modified by the server as ```scope = wallet:read```\"   
## JSON-RPC over websocket 
Websocket is the prefered transport mechanism for the JSON-RPC API, because it is faster and because it can support [subscriptions](#subscriptions) and [cancel on disconnect](#private-enable_cancel_on_disconnect). The code examples that can be found next to each of the methods show how websockets can be used from Python or Javascript/node.js.  
## JSON-RPC over HTTP 
Besides websockets it is also possible to use the API via HTTP. The code examples for 'shell' show how this can be done using curl. Note that subscriptions and cancel on disconnect are not supported via HTTP.  

#Methods 

This Python package is automatically generated by the [OpenAPI Generator](https://openapi-generator.tech) project:

- API version: 2.0.0
- Package version: 1.0.0
- Build package: org.openapitools.codegen.languages.PythonClientCodegen

## Requirements.

Python 2.7 and 3.4+

## Installation & Usage
### pip install

If the python package is hosted on Github, you can install directly from Github

```sh
pip install git+https://github.com/GIT_USER_ID/GIT_REPO_ID.git
```
(you may need to run `pip` with root permission: `sudo pip install git+https://github.com/GIT_USER_ID/GIT_REPO_ID.git`)

Then import the package:
```python
import openapi_client 
```

### Setuptools

Install via [Setuptools](http://pypi.python.org/pypi/setuptools).

```sh
python setup.py install --user
```
(or `sudo python setup.py install` to install the package for all users)

Then import the package:
```python
import openapi_client
```

## Getting Started

Please follow the [installation procedure](#installation--usage) and then run the following:

```python
from __future__ import print_function
import time
import openapi_client
from openapi_client.rest import ApiException
from pprint import pprint

configuration = openapi_client.Configuration()
# Configure Bearer authorization (Auth. Token): bearerAuth
configuration.access_token = 'YOUR_BEARER_TOKEN'

# create an instance of the API class
api_instance = openapi_client.AccountManagementApi(openapi_client.ApiClient(configuration))
sid = 56 # int | The user id for the subaccount
name = 'newUserName' # str | The new user name

try:
    # Change the user name for a subaccount
    api_response = api_instance.private_change_subaccount_name_get(sid, name)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling AccountManagementApi->private_change_subaccount_name_get: %s\n" % e)

```

## Documentation for API Endpoints

All URIs are relative to *https://www.deribit.com/api/v2*

Class | Method | HTTP request | Description
------------ | ------------- | ------------- | -------------
*AccountManagementApi* | [**private_change_subaccount_name_get**](docs/AccountManagementApi.md#private_change_subaccount_name_get) | **GET** /private/change_subaccount_name | Change the user name for a subaccount
*AccountManagementApi* | [**private_create_subaccount_get**](docs/AccountManagementApi.md#private_create_subaccount_get) | **GET** /private/create_subaccount | Create a new subaccount
*AccountManagementApi* | [**private_disable_tfa_for_subaccount_get**](docs/AccountManagementApi.md#private_disable_tfa_for_subaccount_get) | **GET** /private/disable_tfa_for_subaccount | Disable two factor authentication for a subaccount.
*AccountManagementApi* | [**private_get_account_summary_get**](docs/AccountManagementApi.md#private_get_account_summary_get) | **GET** /private/get_account_summary | Retrieves user account summary.
*AccountManagementApi* | [**private_get_email_language_get**](docs/AccountManagementApi.md#private_get_email_language_get) | **GET** /private/get_email_language | Retrieves the language to be used for emails.
*AccountManagementApi* | [**private_get_new_announcements_get**](docs/AccountManagementApi.md#private_get_new_announcements_get) | **GET** /private/get_new_announcements | Retrieves announcements that have not been marked read by the user.
*AccountManagementApi* | [**private_get_position_get**](docs/AccountManagementApi.md#private_get_position_get) | **GET** /private/get_position | Retrieve user position.
*AccountManagementApi* | [**private_get_positions_get**](docs/AccountManagementApi.md#private_get_positions_get) | **GET** /private/get_positions | Retrieve user positions.
*AccountManagementApi* | [**private_get_subaccounts_get**](docs/AccountManagementApi.md#private_get_subaccounts_get) | **GET** /private/get_subaccounts | Get information about subaccounts
*AccountManagementApi* | [**private_set_announcement_as_read_get**](docs/AccountManagementApi.md#private_set_announcement_as_read_get) | **GET** /private/set_announcement_as_read | Marks an announcement as read, so it will not be shown in &#x60;get_new_announcements&#x60;.
*AccountManagementApi* | [**private_set_email_for_subaccount_get**](docs/AccountManagementApi.md#private_set_email_for_subaccount_get) | **GET** /private/set_email_for_subaccount | Assign an email address to a subaccount. User will receive an email with confirmation link.
*AccountManagementApi* | [**private_set_email_language_get**](docs/AccountManagementApi.md#private_set_email_language_get) | **GET** /private/set_email_language | Changes the language to be used for emails.
*AccountManagementApi* | [**private_set_password_for_subaccount_get**](docs/AccountManagementApi.md#private_set_password_for_subaccount_get) | **GET** /private/set_password_for_subaccount | Set the password for the subaccount
*AccountManagementApi* | [**private_toggle_notifications_from_subaccount_get**](docs/AccountManagementApi.md#private_toggle_notifications_from_subaccount_get) | **GET** /private/toggle_notifications_from_subaccount | Enable or disable sending of notifications for the subaccount.
*AccountManagementApi* | [**private_toggle_subaccount_login_get**](docs/AccountManagementApi.md#private_toggle_subaccount_login_get) | **GET** /private/toggle_subaccount_login | Enable or disable login for a subaccount. If login is disabled and a session for the subaccount exists, this session will be terminated.
*AccountManagementApi* | [**public_get_announcements_get**](docs/AccountManagementApi.md#public_get_announcements_get) | **GET** /public/get_announcements | Retrieves announcements from the last 30 days.
*AuthenticationApi* | [**public_auth_get**](docs/AuthenticationApi.md#public_auth_get) | **GET** /public/auth | Authenticate
*InternalApi* | [**private_add_to_address_book_get**](docs/InternalApi.md#private_add_to_address_book_get) | **GET** /private/add_to_address_book | Adds new entry to address book of given type
*InternalApi* | [**private_disable_tfa_with_recovery_code_get**](docs/InternalApi.md#private_disable_tfa_with_recovery_code_get) | **GET** /private/disable_tfa_with_recovery_code | Disables TFA with one time recovery code
*InternalApi* | [**private_get_address_book_get**](docs/InternalApi.md#private_get_address_book_get) | **GET** /private/get_address_book | Retrieves address book of given type
*InternalApi* | [**private_remove_from_address_book_get**](docs/InternalApi.md#private_remove_from_address_book_get) | **GET** /private/remove_from_address_book | Adds new entry to address book of given type
*InternalApi* | [**private_submit_transfer_to_subaccount_get**](docs/InternalApi.md#private_submit_transfer_to_subaccount_get) | **GET** /private/submit_transfer_to_subaccount | Transfer funds to a subaccount.
*InternalApi* | [**private_submit_transfer_to_user_get**](docs/InternalApi.md#private_submit_transfer_to_user_get) | **GET** /private/submit_transfer_to_user | Transfer funds to a another user.
*InternalApi* | [**private_toggle_deposit_address_creation_get**](docs/InternalApi.md#private_toggle_deposit_address_creation_get) | **GET** /private/toggle_deposit_address_creation | Enable or disable deposit address creation
*InternalApi* | [**public_get_footer_get**](docs/InternalApi.md#public_get_footer_get) | **GET** /public/get_footer | Get information to be displayed in the footer of the website.
*InternalApi* | [**public_get_option_mark_prices_get**](docs/InternalApi.md#public_get_option_mark_prices_get) | **GET** /public/get_option_mark_prices | Retrives market prices and its implied volatility of options instruments
*InternalApi* | [**public_validate_field_get**](docs/InternalApi.md#public_validate_field_get) | **GET** /public/validate_field | Method used to introduce the client software connected to Deribit platform over websocket. Provided data may have an impact on the maintained connection and will be collected for internal statistical purposes. In response, Deribit will also introduce itself.
*MarketDataApi* | [**public_get_book_summary_by_currency_get**](docs/MarketDataApi.md#public_get_book_summary_by_currency_get) | **GET** /public/get_book_summary_by_currency | Retrieves the summary information such as open interest, 24h volume, etc. for all instruments for the currency (optionally filtered by kind).
*MarketDataApi* | [**public_get_book_summary_by_instrument_get**](docs/MarketDataApi.md#public_get_book_summary_by_instrument_get) | **GET** /public/get_book_summary_by_instrument | Retrieves the summary information such as open interest, 24h volume, etc. for a specific instrument.
*MarketDataApi* | [**public_get_contract_size_get**](docs/MarketDataApi.md#public_get_contract_size_get) | **GET** /public/get_contract_size | Retrieves contract size of provided instrument.
*MarketDataApi* | [**public_get_currencies_get**](docs/MarketDataApi.md#public_get_currencies_get) | **GET** /public/get_currencies | Retrieves all cryptocurrencies supported by the API.
*MarketDataApi* | [**public_get_funding_chart_data_get**](docs/MarketDataApi.md#public_get_funding_chart_data_get) | **GET** /public/get_funding_chart_data | Retrieve the latest user trades that have occurred for PERPETUAL instruments in a specific currency symbol and within given time range.
*MarketDataApi* | [**public_get_historical_volatility_get**](docs/MarketDataApi.md#public_get_historical_volatility_get) | **GET** /public/get_historical_volatility | Provides information about historical volatility for given cryptocurrency.
*MarketDataApi* | [**public_get_index_get**](docs/MarketDataApi.md#public_get_index_get) | **GET** /public/get_index | Retrieves the current index price for the instruments, for the selected currency.
*MarketDataApi* | [**public_get_instruments_get**](docs/MarketDataApi.md#public_get_instruments_get) | **GET** /public/get_instruments | Retrieves available trading instruments. This method can be used to see which instruments are available for trading, or which instruments have existed historically.
*MarketDataApi* | [**public_get_last_settlements_by_currency_get**](docs/MarketDataApi.md#public_get_last_settlements_by_currency_get) | **GET** /public/get_last_settlements_by_currency | Retrieves historical settlement, delivery and bankruptcy events coming from all instruments within given currency.
*MarketDataApi* | [**public_get_last_settlements_by_instrument_get**](docs/MarketDataApi.md#public_get_last_settlements_by_instrument_get) | **GET** /public/get_last_settlements_by_instrument | Retrieves historical public settlement, delivery and bankruptcy events filtered by instrument name.
*MarketDataApi* | [**public_get_last_trades_by_currency_and_time_get**](docs/MarketDataApi.md#public_get_last_trades_by_currency_and_time_get) | **GET** /public/get_last_trades_by_currency_and_time | Retrieve the latest trades that have occurred for instruments in a specific currency symbol and within given time range.
*MarketDataApi* | [**public_get_last_trades_by_currency_get**](docs/MarketDataApi.md#public_get_last_trades_by_currency_get) | **GET** /public/get_last_trades_by_currency | Retrieve the latest trades that have occurred for instruments in a specific currency symbol.
*MarketDataApi* | [**public_get_last_trades_by_instrument_and_time_get**](docs/MarketDataApi.md#public_get_last_trades_by_instrument_and_time_get) | **GET** /public/get_last_trades_by_instrument_and_time | Retrieve the latest trades that have occurred for a specific instrument and within given time range.
*MarketDataApi* | [**public_get_last_trades_by_instrument_get**](docs/MarketDataApi.md#public_get_last_trades_by_instrument_get) | **GET** /public/get_last_trades_by_instrument | Retrieve the latest trades that have occurred for a specific instrument.
*MarketDataApi* | [**public_get_order_book_get**](docs/MarketDataApi.md#public_get_order_book_get) | **GET** /public/get_order_book | Retrieves the order book, along with other market values for a given instrument.
*MarketDataApi* | [**public_get_trade_volumes_get**](docs/MarketDataApi.md#public_get_trade_volumes_get) | **GET** /public/get_trade_volumes | Retrieves aggregated 24h trade volumes for different instrument types and currencies.
*MarketDataApi* | [**public_get_tradingview_chart_data_get**](docs/MarketDataApi.md#public_get_tradingview_chart_data_get) | **GET** /public/get_tradingview_chart_data | Publicly available market data used to generate a TradingView candle chart.
*MarketDataApi* | [**public_ticker_get**](docs/MarketDataApi.md#public_ticker_get) | **GET** /public/ticker | Get ticker for an instrument.
*PrivateApi* | [**private_add_to_address_book_get**](docs/PrivateApi.md#private_add_to_address_book_get) | **GET** /private/add_to_address_book | Adds new entry to address book of given type
*PrivateApi* | [**private_buy_get**](docs/PrivateApi.md#private_buy_get) | **GET** /private/buy | Places a buy order for an instrument.
*PrivateApi* | [**private_cancel_all_by_currency_get**](docs/PrivateApi.md#private_cancel_all_by_currency_get) | **GET** /private/cancel_all_by_currency | Cancels all orders by currency, optionally filtered by instrument kind and/or order type.
*PrivateApi* | [**private_cancel_all_by_instrument_get**](docs/PrivateApi.md#private_cancel_all_by_instrument_get) | **GET** /private/cancel_all_by_instrument | Cancels all orders by instrument, optionally filtered by order type.
*PrivateApi* | [**private_cancel_all_get**](docs/PrivateApi.md#private_cancel_all_get) | **GET** /private/cancel_all | This method cancels all users orders and stop orders within all currencies and instrument kinds.
*PrivateApi* | [**private_cancel_get**](docs/PrivateApi.md#private_cancel_get) | **GET** /private/cancel | Cancel an order, specified by order id
*PrivateApi* | [**private_cancel_transfer_by_id_get**](docs/PrivateApi.md#private_cancel_transfer_by_id_get) | **GET** /private/cancel_transfer_by_id | Cancel transfer
*PrivateApi* | [**private_cancel_withdrawal_get**](docs/PrivateApi.md#private_cancel_withdrawal_get) | **GET** /private/cancel_withdrawal | Cancels withdrawal request
*PrivateApi* | [**private_change_subaccount_name_get**](docs/PrivateApi.md#private_change_subaccount_name_get) | **GET** /private/change_subaccount_name | Change the user name for a subaccount
*PrivateApi* | [**private_close_position_get**](docs/PrivateApi.md#private_close_position_get) | **GET** /private/close_position | Makes closing position reduce only order .
*PrivateApi* | [**private_create_deposit_address_get**](docs/PrivateApi.md#private_create_deposit_address_get) | **GET** /private/create_deposit_address | Creates deposit address in currency
*PrivateApi* | [**private_create_subaccount_get**](docs/PrivateApi.md#private_create_subaccount_get) | **GET** /private/create_subaccount | Create a new subaccount
*PrivateApi* | [**private_disable_tfa_for_subaccount_get**](docs/PrivateApi.md#private_disable_tfa_for_subaccount_get) | **GET** /private/disable_tfa_for_subaccount | Disable two factor authentication for a subaccount.
*PrivateApi* | [**private_disable_tfa_with_recovery_code_get**](docs/PrivateApi.md#private_disable_tfa_with_recovery_code_get) | **GET** /private/disable_tfa_with_recovery_code | Disables TFA with one time recovery code
*PrivateApi* | [**private_edit_get**](docs/PrivateApi.md#private_edit_get) | **GET** /private/edit | Change price, amount and/or other properties of an order.
*PrivateApi* | [**private_get_account_summary_get**](docs/PrivateApi.md#private_get_account_summary_get) | **GET** /private/get_account_summary | Retrieves user account summary.
*PrivateApi* | [**private_get_address_book_get**](docs/PrivateApi.md#private_get_address_book_get) | **GET** /private/get_address_book | Retrieves address book of given type
*PrivateApi* | [**private_get_current_deposit_address_get**](docs/PrivateApi.md#private_get_current_deposit_address_get) | **GET** /private/get_current_deposit_address | Retrieve deposit address for currency
*PrivateApi* | [**private_get_deposits_get**](docs/PrivateApi.md#private_get_deposits_get) | **GET** /private/get_deposits | Retrieve the latest users deposits
*PrivateApi* | [**private_get_email_language_get**](docs/PrivateApi.md#private_get_email_language_get) | **GET** /private/get_email_language | Retrieves the language to be used for emails.
*PrivateApi* | [**private_get_margins_get**](docs/PrivateApi.md#private_get_margins_get) | **GET** /private/get_margins | Get margins for given instrument, amount and price.
*PrivateApi* | [**private_get_new_announcements_get**](docs/PrivateApi.md#private_get_new_announcements_get) | **GET** /private/get_new_announcements | Retrieves announcements that have not been marked read by the user.
*PrivateApi* | [**private_get_open_orders_by_currency_get**](docs/PrivateApi.md#private_get_open_orders_by_currency_get) | **GET** /private/get_open_orders_by_currency | Retrieves list of user&#39;s open orders.
*PrivateApi* | [**private_get_open_orders_by_instrument_get**](docs/PrivateApi.md#private_get_open_orders_by_instrument_get) | **GET** /private/get_open_orders_by_instrument | Retrieves list of user&#39;s open orders within given Instrument.
*PrivateApi* | [**private_get_order_history_by_currency_get**](docs/PrivateApi.md#private_get_order_history_by_currency_get) | **GET** /private/get_order_history_by_currency | Retrieves history of orders that have been partially or fully filled.
*PrivateApi* | [**private_get_order_history_by_instrument_get**](docs/PrivateApi.md#private_get_order_history_by_instrument_get) | **GET** /private/get_order_history_by_instrument | Retrieves history of orders that have been partially or fully filled.
*PrivateApi* | [**private_get_order_margin_by_ids_get**](docs/PrivateApi.md#private_get_order_margin_by_ids_get) | **GET** /private/get_order_margin_by_ids | Retrieves initial margins of given orders
*PrivateApi* | [**private_get_order_state_get**](docs/PrivateApi.md#private_get_order_state_get) | **GET** /private/get_order_state | Retrieve the current state of an order.
*PrivateApi* | [**private_get_position_get**](docs/PrivateApi.md#private_get_position_get) | **GET** /private/get_position | Retrieve user position.
*PrivateApi* | [**private_get_positions_get**](docs/PrivateApi.md#private_get_positions_get) | **GET** /private/get_positions | Retrieve user positions.
*PrivateApi* | [**private_get_settlement_history_by_currency_get**](docs/PrivateApi.md#private_get_settlement_history_by_currency_get) | **GET** /private/get_settlement_history_by_currency | Retrieves settlement, delivery and bankruptcy events that have affected your account.
*PrivateApi* | [**private_get_settlement_history_by_instrument_get**](docs/PrivateApi.md#private_get_settlement_history_by_instrument_get) | **GET** /private/get_settlement_history_by_instrument | Retrieves public settlement, delivery and bankruptcy events filtered by instrument name
*PrivateApi* | [**private_get_subaccounts_get**](docs/PrivateApi.md#private_get_subaccounts_get) | **GET** /private/get_subaccounts | Get information about subaccounts
*PrivateApi* | [**private_get_transfers_get**](docs/PrivateApi.md#private_get_transfers_get) | **GET** /private/get_transfers | Adds new entry to address book of given type
*PrivateApi* | [**private_get_user_trades_by_currency_and_time_get**](docs/PrivateApi.md#private_get_user_trades_by_currency_and_time_get) | **GET** /private/get_user_trades_by_currency_and_time | Retrieve the latest user trades that have occurred for instruments in a specific currency symbol and within given time range.
*PrivateApi* | [**private_get_user_trades_by_currency_get**](docs/PrivateApi.md#private_get_user_trades_by_currency_get) | **GET** /private/get_user_trades_by_currency | Retrieve the latest user trades that have occurred for instruments in a specific currency symbol.
*PrivateApi* | [**private_get_user_trades_by_instrument_and_time_get**](docs/PrivateApi.md#private_get_user_trades_by_instrument_and_time_get) | **GET** /private/get_user_trades_by_instrument_and_time | Retrieve the latest user trades that have occurred for a specific instrument and within given time range.
*PrivateApi* | [**private_get_user_trades_by_instrument_get**](docs/PrivateApi.md#private_get_user_trades_by_instrument_get) | **GET** /private/get_user_trades_by_instrument | Retrieve the latest user trades that have occurred for a specific instrument.
*PrivateApi* | [**private_get_user_trades_by_order_get**](docs/PrivateApi.md#private_get_user_trades_by_order_get) | **GET** /private/get_user_trades_by_order | Retrieve the list of user trades that was created for given order
*PrivateApi* | [**private_get_withdrawals_get**](docs/PrivateApi.md#private_get_withdrawals_get) | **GET** /private/get_withdrawals | Retrieve the latest users withdrawals
*PrivateApi* | [**private_remove_from_address_book_get**](docs/PrivateApi.md#private_remove_from_address_book_get) | **GET** /private/remove_from_address_book | Adds new entry to address book of given type
*PrivateApi* | [**private_sell_get**](docs/PrivateApi.md#private_sell_get) | **GET** /private/sell | Places a sell order for an instrument.
*PrivateApi* | [**private_set_announcement_as_read_get**](docs/PrivateApi.md#private_set_announcement_as_read_get) | **GET** /private/set_announcement_as_read | Marks an announcement as read, so it will not be shown in &#x60;get_new_announcements&#x60;.
*PrivateApi* | [**private_set_email_for_subaccount_get**](docs/PrivateApi.md#private_set_email_for_subaccount_get) | **GET** /private/set_email_for_subaccount | Assign an email address to a subaccount. User will receive an email with confirmation link.
*PrivateApi* | [**private_set_email_language_get**](docs/PrivateApi.md#private_set_email_language_get) | **GET** /private/set_email_language | Changes the language to be used for emails.
*PrivateApi* | [**private_set_password_for_subaccount_get**](docs/PrivateApi.md#private_set_password_for_subaccount_get) | **GET** /private/set_password_for_subaccount | Set the password for the subaccount
*PrivateApi* | [**private_submit_transfer_to_subaccount_get**](docs/PrivateApi.md#private_submit_transfer_to_subaccount_get) | **GET** /private/submit_transfer_to_subaccount | Transfer funds to a subaccount.
*PrivateApi* | [**private_submit_transfer_to_user_get**](docs/PrivateApi.md#private_submit_transfer_to_user_get) | **GET** /private/submit_transfer_to_user | Transfer funds to a another user.
*PrivateApi* | [**private_toggle_deposit_address_creation_get**](docs/PrivateApi.md#private_toggle_deposit_address_creation_get) | **GET** /private/toggle_deposit_address_creation | Enable or disable deposit address creation
*PrivateApi* | [**private_toggle_notifications_from_subaccount_get**](docs/PrivateApi.md#private_toggle_notifications_from_subaccount_get) | **GET** /private/toggle_notifications_from_subaccount | Enable or disable sending of notifications for the subaccount.
*PrivateApi* | [**private_toggle_subaccount_login_get**](docs/PrivateApi.md#private_toggle_subaccount_login_get) | **GET** /private/toggle_subaccount_login | Enable or disable login for a subaccount. If login is disabled and a session for the subaccount exists, this session will be terminated.
*PrivateApi* | [**private_withdraw_get**](docs/PrivateApi.md#private_withdraw_get) | **GET** /private/withdraw | Creates a new withdrawal request
*PublicApi* | [**public_auth_get**](docs/PublicApi.md#public_auth_get) | **GET** /public/auth | Authenticate
*PublicApi* | [**public_get_announcements_get**](docs/PublicApi.md#public_get_announcements_get) | **GET** /public/get_announcements | Retrieves announcements from the last 30 days.
*PublicApi* | [**public_get_book_summary_by_currency_get**](docs/PublicApi.md#public_get_book_summary_by_currency_get) | **GET** /public/get_book_summary_by_currency | Retrieves the summary information such as open interest, 24h volume, etc. for all instruments for the currency (optionally filtered by kind).
*PublicApi* | [**public_get_book_summary_by_instrument_get**](docs/PublicApi.md#public_get_book_summary_by_instrument_get) | **GET** /public/get_book_summary_by_instrument | Retrieves the summary information such as open interest, 24h volume, etc. for a specific instrument.
*PublicApi* | [**public_get_contract_size_get**](docs/PublicApi.md#public_get_contract_size_get) | **GET** /public/get_contract_size | Retrieves contract size of provided instrument.
*PublicApi* | [**public_get_currencies_get**](docs/PublicApi.md#public_get_currencies_get) | **GET** /public/get_currencies | Retrieves all cryptocurrencies supported by the API.
*PublicApi* | [**public_get_funding_chart_data_get**](docs/PublicApi.md#public_get_funding_chart_data_get) | **GET** /public/get_funding_chart_data | Retrieve the latest user trades that have occurred for PERPETUAL instruments in a specific currency symbol and within given time range.
*PublicApi* | [**public_get_historical_volatility_get**](docs/PublicApi.md#public_get_historical_volatility_get) | **GET** /public/get_historical_volatility | Provides information about historical volatility for given cryptocurrency.
*PublicApi* | [**public_get_index_get**](docs/PublicApi.md#public_get_index_get) | **GET** /public/get_index | Retrieves the current index price for the instruments, for the selected currency.
*PublicApi* | [**public_get_instruments_get**](docs/PublicApi.md#public_get_instruments_get) | **GET** /public/get_instruments | Retrieves available trading instruments. This method can be used to see which instruments are available for trading, or which instruments have existed historically.
*PublicApi* | [**public_get_last_settlements_by_currency_get**](docs/PublicApi.md#public_get_last_settlements_by_currency_get) | **GET** /public/get_last_settlements_by_currency | Retrieves historical settlement, delivery and bankruptcy events coming from all instruments within given currency.
*PublicApi* | [**public_get_last_settlements_by_instrument_get**](docs/PublicApi.md#public_get_last_settlements_by_instrument_get) | **GET** /public/get_last_settlements_by_instrument | Retrieves historical public settlement, delivery and bankruptcy events filtered by instrument name.
*PublicApi* | [**public_get_last_trades_by_currency_and_time_get**](docs/PublicApi.md#public_get_last_trades_by_currency_and_time_get) | **GET** /public/get_last_trades_by_currency_and_time | Retrieve the latest trades that have occurred for instruments in a specific currency symbol and within given time range.
*PublicApi* | [**public_get_last_trades_by_currency_get**](docs/PublicApi.md#public_get_last_trades_by_currency_get) | **GET** /public/get_last_trades_by_currency | Retrieve the latest trades that have occurred for instruments in a specific currency symbol.
*PublicApi* | [**public_get_last_trades_by_instrument_and_time_get**](docs/PublicApi.md#public_get_last_trades_by_instrument_and_time_get) | **GET** /public/get_last_trades_by_instrument_and_time | Retrieve the latest trades that have occurred for a specific instrument and within given time range.
*PublicApi* | [**public_get_last_trades_by_instrument_get**](docs/PublicApi.md#public_get_last_trades_by_instrument_get) | **GET** /public/get_last_trades_by_instrument | Retrieve the latest trades that have occurred for a specific instrument.
*PublicApi* | [**public_get_order_book_get**](docs/PublicApi.md#public_get_order_book_get) | **GET** /public/get_order_book | Retrieves the order book, along with other market values for a given instrument.
*PublicApi* | [**public_get_time_get**](docs/PublicApi.md#public_get_time_get) | **GET** /public/get_time | Retrieves the current time (in milliseconds). This API endpoint can be used to check the clock skew between your software and Deribit&#39;s systems.
*PublicApi* | [**public_get_trade_volumes_get**](docs/PublicApi.md#public_get_trade_volumes_get) | **GET** /public/get_trade_volumes | Retrieves aggregated 24h trade volumes for different instrument types and currencies.
*PublicApi* | [**public_get_tradingview_chart_data_get**](docs/PublicApi.md#public_get_tradingview_chart_data_get) | **GET** /public/get_tradingview_chart_data | Publicly available market data used to generate a TradingView candle chart.
*PublicApi* | [**public_test_get**](docs/PublicApi.md#public_test_get) | **GET** /public/test | Tests the connection to the API server, and returns its version. You can use this to make sure the API is reachable, and matches the expected version.
*PublicApi* | [**public_ticker_get**](docs/PublicApi.md#public_ticker_get) | **GET** /public/ticker | Get ticker for an instrument.
*PublicApi* | [**public_validate_field_get**](docs/PublicApi.md#public_validate_field_get) | **GET** /public/validate_field | Method used to introduce the client software connected to Deribit platform over websocket. Provided data may have an impact on the maintained connection and will be collected for internal statistical purposes. In response, Deribit will also introduce itself.
*SupportingApi* | [**public_get_time_get**](docs/SupportingApi.md#public_get_time_get) | **GET** /public/get_time | Retrieves the current time (in milliseconds). This API endpoint can be used to check the clock skew between your software and Deribit&#39;s systems.
*SupportingApi* | [**public_test_get**](docs/SupportingApi.md#public_test_get) | **GET** /public/test | Tests the connection to the API server, and returns its version. You can use this to make sure the API is reachable, and matches the expected version.
*TradingApi* | [**private_buy_get**](docs/TradingApi.md#private_buy_get) | **GET** /private/buy | Places a buy order for an instrument.
*TradingApi* | [**private_cancel_all_by_currency_get**](docs/TradingApi.md#private_cancel_all_by_currency_get) | **GET** /private/cancel_all_by_currency | Cancels all orders by currency, optionally filtered by instrument kind and/or order type.
*TradingApi* | [**private_cancel_all_by_instrument_get**](docs/TradingApi.md#private_cancel_all_by_instrument_get) | **GET** /private/cancel_all_by_instrument | Cancels all orders by instrument, optionally filtered by order type.
*TradingApi* | [**private_cancel_all_get**](docs/TradingApi.md#private_cancel_all_get) | **GET** /private/cancel_all | This method cancels all users orders and stop orders within all currencies and instrument kinds.
*TradingApi* | [**private_cancel_get**](docs/TradingApi.md#private_cancel_get) | **GET** /private/cancel | Cancel an order, specified by order id
*TradingApi* | [**private_close_position_get**](docs/TradingApi.md#private_close_position_get) | **GET** /private/close_position | Makes closing position reduce only order .
*TradingApi* | [**private_edit_get**](docs/TradingApi.md#private_edit_get) | **GET** /private/edit | Change price, amount and/or other properties of an order.
*TradingApi* | [**private_get_margins_get**](docs/TradingApi.md#private_get_margins_get) | **GET** /private/get_margins | Get margins for given instrument, amount and price.
*TradingApi* | [**private_get_open_orders_by_currency_get**](docs/TradingApi.md#private_get_open_orders_by_currency_get) | **GET** /private/get_open_orders_by_currency | Retrieves list of user&#39;s open orders.
*TradingApi* | [**private_get_open_orders_by_instrument_get**](docs/TradingApi.md#private_get_open_orders_by_instrument_get) | **GET** /private/get_open_orders_by_instrument | Retrieves list of user&#39;s open orders within given Instrument.
*TradingApi* | [**private_get_order_history_by_currency_get**](docs/TradingApi.md#private_get_order_history_by_currency_get) | **GET** /private/get_order_history_by_currency | Retrieves history of orders that have been partially or fully filled.
*TradingApi* | [**private_get_order_history_by_instrument_get**](docs/TradingApi.md#private_get_order_history_by_instrument_get) | **GET** /private/get_order_history_by_instrument | Retrieves history of orders that have been partially or fully filled.
*TradingApi* | [**private_get_order_margin_by_ids_get**](docs/TradingApi.md#private_get_order_margin_by_ids_get) | **GET** /private/get_order_margin_by_ids | Retrieves initial margins of given orders
*TradingApi* | [**private_get_order_state_get**](docs/TradingApi.md#private_get_order_state_get) | **GET** /private/get_order_state | Retrieve the current state of an order.
*TradingApi* | [**private_get_settlement_history_by_currency_get**](docs/TradingApi.md#private_get_settlement_history_by_currency_get) | **GET** /private/get_settlement_history_by_currency | Retrieves settlement, delivery and bankruptcy events that have affected your account.
*TradingApi* | [**private_get_settlement_history_by_instrument_get**](docs/TradingApi.md#private_get_settlement_history_by_instrument_get) | **GET** /private/get_settlement_history_by_instrument | Retrieves public settlement, delivery and bankruptcy events filtered by instrument name
*TradingApi* | [**private_get_user_trades_by_currency_and_time_get**](docs/TradingApi.md#private_get_user_trades_by_currency_and_time_get) | **GET** /private/get_user_trades_by_currency_and_time | Retrieve the latest user trades that have occurred for instruments in a specific currency symbol and within given time range.
*TradingApi* | [**private_get_user_trades_by_currency_get**](docs/TradingApi.md#private_get_user_trades_by_currency_get) | **GET** /private/get_user_trades_by_currency | Retrieve the latest user trades that have occurred for instruments in a specific currency symbol.
*TradingApi* | [**private_get_user_trades_by_instrument_and_time_get**](docs/TradingApi.md#private_get_user_trades_by_instrument_and_time_get) | **GET** /private/get_user_trades_by_instrument_and_time | Retrieve the latest user trades that have occurred for a specific instrument and within given time range.
*TradingApi* | [**private_get_user_trades_by_instrument_get**](docs/TradingApi.md#private_get_user_trades_by_instrument_get) | **GET** /private/get_user_trades_by_instrument | Retrieve the latest user trades that have occurred for a specific instrument.
*TradingApi* | [**private_get_user_trades_by_order_get**](docs/TradingApi.md#private_get_user_trades_by_order_get) | **GET** /private/get_user_trades_by_order | Retrieve the list of user trades that was created for given order
*TradingApi* | [**private_sell_get**](docs/TradingApi.md#private_sell_get) | **GET** /private/sell | Places a sell order for an instrument.
*WalletApi* | [**private_add_to_address_book_get**](docs/WalletApi.md#private_add_to_address_book_get) | **GET** /private/add_to_address_book | Adds new entry to address book of given type
*WalletApi* | [**private_cancel_transfer_by_id_get**](docs/WalletApi.md#private_cancel_transfer_by_id_get) | **GET** /private/cancel_transfer_by_id | Cancel transfer
*WalletApi* | [**private_cancel_withdrawal_get**](docs/WalletApi.md#private_cancel_withdrawal_get) | **GET** /private/cancel_withdrawal | Cancels withdrawal request
*WalletApi* | [**private_create_deposit_address_get**](docs/WalletApi.md#private_create_deposit_address_get) | **GET** /private/create_deposit_address | Creates deposit address in currency
*WalletApi* | [**private_get_address_book_get**](docs/WalletApi.md#private_get_address_book_get) | **GET** /private/get_address_book | Retrieves address book of given type
*WalletApi* | [**private_get_current_deposit_address_get**](docs/WalletApi.md#private_get_current_deposit_address_get) | **GET** /private/get_current_deposit_address | Retrieve deposit address for currency
*WalletApi* | [**private_get_deposits_get**](docs/WalletApi.md#private_get_deposits_get) | **GET** /private/get_deposits | Retrieve the latest users deposits
*WalletApi* | [**private_get_transfers_get**](docs/WalletApi.md#private_get_transfers_get) | **GET** /private/get_transfers | Adds new entry to address book of given type
*WalletApi* | [**private_get_withdrawals_get**](docs/WalletApi.md#private_get_withdrawals_get) | **GET** /private/get_withdrawals | Retrieve the latest users withdrawals
*WalletApi* | [**private_remove_from_address_book_get**](docs/WalletApi.md#private_remove_from_address_book_get) | **GET** /private/remove_from_address_book | Adds new entry to address book of given type
*WalletApi* | [**private_submit_transfer_to_subaccount_get**](docs/WalletApi.md#private_submit_transfer_to_subaccount_get) | **GET** /private/submit_transfer_to_subaccount | Transfer funds to a subaccount.
*WalletApi* | [**private_submit_transfer_to_user_get**](docs/WalletApi.md#private_submit_transfer_to_user_get) | **GET** /private/submit_transfer_to_user | Transfer funds to a another user.
*WalletApi* | [**private_toggle_deposit_address_creation_get**](docs/WalletApi.md#private_toggle_deposit_address_creation_get) | **GET** /private/toggle_deposit_address_creation | Enable or disable deposit address creation
*WalletApi* | [**private_withdraw_get**](docs/WalletApi.md#private_withdraw_get) | **GET** /private/withdraw | Creates a new withdrawal request


## Documentation For Models

 - [AddressBookItem](docs/AddressBookItem.md)
 - [BookSummary](docs/BookSummary.md)
 - [Currency](docs/Currency.md)
 - [CurrencyPortfolio](docs/CurrencyPortfolio.md)
 - [CurrencyWithdrawalPriorities](docs/CurrencyWithdrawalPriorities.md)
 - [Deposit](docs/Deposit.md)
 - [Instrument](docs/Instrument.md)
 - [KeyNumberPair](docs/KeyNumberPair.md)
 - [Order](docs/Order.md)
 - [OrderIdInitialMarginPair](docs/OrderIdInitialMarginPair.md)
 - [Portfolio](docs/Portfolio.md)
 - [PortfolioEth](docs/PortfolioEth.md)
 - [Position](docs/Position.md)
 - [PublicTrade](docs/PublicTrade.md)
 - [Settlement](docs/Settlement.md)
 - [TradesVolumes](docs/TradesVolumes.md)
 - [TransferItem](docs/TransferItem.md)
 - [Types](docs/Types.md)
 - [UserTrade](docs/UserTrade.md)
 - [Withdrawal](docs/Withdrawal.md)


## Documentation For Authorization


## bearerAuth

- **Type**: Bearer authentication (Auth. Token)


## Author




