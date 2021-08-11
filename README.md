
# ODD Platform Puller

ODD Platform Puller service is a hub between [ODD Platform](https://github.com/opendatadiscovery/odd-platform) and "Pull" adapters.

## Configuration

ODD Platform Puller service can be configured via `application.yaml` file or environment variables.

| Property | Variable type | Description | Default value |  
|--|--|--|--|  
| PLATFORM_HOST_URL | URL | ODD Platform's host URL, e.g. `http://localhost:8080` | - |  
| PLATFORM_FETCHING_DELAY | Duration | Periodic interval on which data sources fetching task is executed.  | 10s |  
| PLATFORM_AUTH_TYPE | String enum (DISABLED, LOGIN_FORM, OAUTH2) | Mechanism to use to authenticate in ODD Platform. Must match with the corresponding Platform's configuration | DISABLED |  
| PLATFORM_AUTH_LOGIN_FORM_CREDENTIALS | String (`username:password` format)  | Credentials for pulling active data source list and ingesting into ODD Platform. Must be specified if `PLATFORM_AUTH_TYPE` setting is `LOGIN_FORM` | `admin:admin` |  
| PLATFORM_AUTH_OAUTH_AUTH_DOMAIN | URL | Host URL of Authorization server. Must be specified if `PLATFORM_AUTH_TYPE` setting is `OAUTH2` | - |  
| PLATFORM_AUTH_OAUTH_CLIENT_ID | String | OAuth2 Client ID. Must be specified if `PLATFORM_AUTH_TYPE` setting is `OAUTH2` | - |  
| PLATFORM_AUTH_OAUTH_CLIENT_SECRET | String | OAuth2 Client Secret. Must be specified if `PLATFORM_AUTH_TYPE` setting is `OAUTH2` | - |  
| PULLER_WORKER_THREAD_POOL_SIZE | Integer | The size of worker thread pool used for pulling entities from adapters and ingesting into ODD Platform | 5 |

## Authentication
Puller service along with ODD Platform supports Login Form and OAuth (client_credentials flow) authentication mechanisms.