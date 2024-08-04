# JWT

## Best Practices

https://blog.logrocket.com/jwt-authentication-best-practices/

## Libraries

Select your language of choice and pick the library that you prefer — ideally, the one with the highest number of green
checks:

https://jwt.io/libraries

## Refresh Tokens

Refreshing expired tokens with JWT typically involves the use of a refresh token mechanism. Refresh tokens are
long-lived tokens that can be used to obtain a new JWT when the original token expires.

Here’s a typical flow for refreshing expired tokens:

Users would log in with their credentials. The server generates both an access token (JWT) and a refresh token. The
access token has a relatively short expiration time while the refresh token has a longer expiration time.

Then, the server sends the JWT and Refresh token to the client. The refresh token is usually stored in a secure cookie.
Each subsequent request from the client includes the JWT.

The user’s identity and authorization details are then extracted from the token, eliminating the need for constant
database lookups.

When the access token expires, the client sends a request to a token refresh endpoint with the refresh token. The server
validates the refresh token. If the refresh token is valid, the server issues a new access token and, optionally, a new
refresh token.

