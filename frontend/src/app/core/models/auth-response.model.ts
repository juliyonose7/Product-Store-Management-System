export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  username: string;
  role: string;
  accessTokenExpiresAt: number;
  refreshTokenExpiresAt: number;
}
