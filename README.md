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

| Variable name        | Description                                       |
| -------------------- | ------------------------------------------------- |
| `VIISP_PID`          | VIISP unique service ID, e.g. `VSID000000006110`  |
| `VIISP_POSTBACK_URL` | VIISP postback URL, e.g. `https://login.biip.lt/` |
| `KEYSTORE_BASE64`    | JKS keystore encoded as base64                    |
| `KEYSTORE_PASSWORD`  | JKS keystore password                             |

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
