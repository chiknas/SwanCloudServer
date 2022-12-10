const { fetch: originalFetch } = window;

let accessToken;

window.fetch = async (...args) => {
  let [resource, config] = args;

  // Send request and return if success
  const response = await originalFetch(resource, {
    ...config,
    headers: new Headers({
      Authorization: `Bearer ${accessToken}`,
    }),
  });
  if (response.status != 403) {
    return response;
  }

  // Refresh access token if request is unaithenticated
  const refreshTokenResponse = await originalFetch("/auth/refreshtoken", {
    method: "POST",
  }).then((r) => r.json());

  accessToken = refreshTokenResponse.accessToken;

  // Resend the original request which will use the new cookies
  return await originalFetch(resource, {
    ...config,
    headers: new Headers({
      Authorization: `Bearer ${accessToken}`,
    }),
  });
};
