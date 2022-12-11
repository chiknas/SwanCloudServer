const { fetch: originalFetch } = window;

let accessToken;

const sendOriginalRequestAuthenticated = async (resource, config) => {
  return await originalFetch(resource, {
    ...config,
    headers: {
      ...config?.headers,
      Authorization: `Bearer ${accessToken}`,
    },
  });
};

window.fetch = async (...args) => {
  let [resource, config] = args;

  // Send request and return if success
  const response = await sendOriginalRequestAuthenticated(resource, config);

  if (response.status != 403 && !resource.includes("/auth/resetpassword")) {
    return response;
  }

  // Refresh access token if request is unaithenticated
  const refreshTokenResponse = await originalFetch("/auth/refreshtoken", {
    method: "POST",
  }).then((r) => r.json());

  accessToken = refreshTokenResponse.accessToken;

  // Resend the original request which will use the new cookies
  return await sendOriginalRequestAuthenticated(resource, config);
};
