# VIISP authentication helper

This helper is used to generate/sign tickets & validate/get data after successful login through
VIISP (https://epaslaugos.lt).

## How it works?

1. Send request to this handler to create a new ticket.
2. Redirect user to `epaslaugos.lt`
3. After successful login, user is redirected to `login.biip.lt` (POST request -
   check [handler](https://github.com/AplinkosMinisterija/login-biip))
4. Login handler redirects user by `customData.host` prop to some other url
5. That endpoint extracts data through API and this handler.

## Endpoints

| Endpoint                    | Description                                                               | Params                                                                                               | Info                                                               | Returns                                                                                                                                     |
| --------------------------- | ------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------- |
| POST `/auth/sign`           | Generates new ticket                                                      | `host` (required) <br> optionally - pass any data and it will be returned in `customData` afterwards | `host` param needed for `login.biip.lt` to redirect signed in user | JSON that contains:<br> 1. `ticket` - generated ticket ID.<br> 2. `host` - epaslaugos url <br> 3. `url` - concatinated url to redirect user |
| GET `/auth/data?ticket=...` | Gets signed in user/company data. Valid to call after successful sign in. | `ticket` (required)                                                                                  | `ticket` has to be previously generated ticket ID.                 | All avilable data such as `firstName`, `lastName`, `lt-personal-code`, etc as JSON.                                                         |

## Environment variables

| Variable name        | Description                                      |
| -------------------- | ------------------------------------------------ |
| `VIISP_PID`          | VIISP unique service ID, e.g. `VSID000000000000` |
| `VIISP_POSTBACK_URL` | VIISP postback URL, e.g. `https://website.com/`  |
| `KEYSTORE_BASE64`    | JKS keystore encoded as base64                   |
| `KEYSTORE_PASSWORD`  | JKS keystore password                            |
| `VIISP_SIGNATURE_ALGORITHM` | `sha256` (default) or `sha1`: algorithm used to sign requests to VIISP (see below) |
| `VIISP_AUTH_SERVICE_URL` | VIISP SOAP authentication service. Default: production, `https://www.epaslaugos.lt/portal/authenticationServices/auth` |
| `VIISP_LOGIN_URL` | VIISP page the user is sent to with the ticket. Default: production, `https://www.epaslaugos.lt/portal/external/services/authentication/v2/` |

### Signature algorithm

VIISP stops accepting requests signed with RSA-SHA1 / SHA-1 in its test environment on 2027-02-01 and in production
on 2027-05-01; from then on requests must be signed with RSA-SHA256 (`SignatureMethod`) and SHA-256 (`DigestMethod`).
The certificate, the keys and the message structure do not change. The helper signs with SHA-256 by default.
`VIISP_SIGNATURE_ALGORITHM=sha1` keeps the old algorithm for an environment that cannot switch yet; remove it before
2027-05-01.

### VIISP test environment

```
VIISP_AUTH_SERVICE_URL=https://test.epaslaugos.lt/services/services/auth
VIISP_LOGIN_URL=https://test.epaslaugos.lt/portal/external/services/authentication/v2/
```

The test environment accepts requests only from IP addresses VSSA has allowed; other addresses get an HTML page
"Užklausa užblokuota" (HTTP 500).

### **Keep in mind:**

**After ticket is generated you can:**

1. Create a form that redirects user to sign in. In example bellow - underscored variables are results from the request.

```html
<form method="POST" action="_host_">
  <input type="hidden" value="_ticket_" name="ticket" />
  <button type="submit">Sign In</button>
</form>
```

2. Redirect user directly to `url` endpoint.

**After successfull sign in** user is redirected to some specific url. This is done by `login.biip.lt`
by `customData.host` prop. `ticket` and `customData` are stored as query parameters after redirect.

## Development

```bash
  docker-compose up --build
  // OR
  mvn spring-boot:run // needs setup to work properly (or hacks)
```

## Useful info

1. [EPaslaugos website (LT)](https://www.epaslaugos.lt/portal/content/1257)
2. [To understand how it works (LT)](http://9v.lt/blog/viisp-tapatybes-nustatymo-paslauga/)
3. Certificates are valid up until 2121

### Converting `jks` to `base64`
💡 Note: replace all `<...>` variables with your own.

1. Convert `jks` to `p12`. 

```shell
keytool -importkeystore -srckeystore <keystore.jks> -destkeystore <keystore.p12> -srcstoretype JKS -deststoretype PKCS12 -deststorepass <p12_password>
```

2. Convert `p12` to `base64`

```shell
base64 -i <keystore.p12>
```

3. Congrats! 🎉 Now use this base64 value as `KEYSTORE_BASE64` and `<p12_password>` as `KEYSTORE_PASSWORD`
