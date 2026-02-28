import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface CreateUserPayload {
  username: string;
  role: 'ADMIN' | 'CAJERO';
  temporaryPassword: string;
}

export interface UserResponse {
  username: string;
  role: string;
  enabled: boolean;
  mustChangePassword: boolean;
}

export interface UsersCountResponse {
  totalUsers: number;
  connectedUsers: number;
}

export interface UserConnectionStatus {
  username: string;
  role: string;
  enabled: boolean;
  connected: boolean;
  lastConnectionAt: string | null;
}

export interface UpdateUserEnabledPayload {
  enabled: boolean;
}

export interface AdminResetPasswordPayload {
  temporaryPassword: string;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly endpoint = `${environment.apiBaseUrl}/users`;

  constructor(private readonly http: HttpClient) {}

  createUser(payload: CreateUserPayload): Observable<UserResponse> {
    return this.http.post<UserResponse>(this.endpoint, payload);
  }

  getUsersCount(): Observable<UsersCountResponse> {
    return this.http.get<UsersCountResponse>(`${this.endpoint}/count`);
  }

  getUsersStatus(): Observable<UserConnectionStatus[]> {
    return this.http.get<UserConnectionStatus[]>(`${this.endpoint}/status`);
  }

  updateUserEnabled(username: string, payload: UpdateUserEnabledPayload): Observable<UserResponse> {
    return this.http.patch<UserResponse>(`${this.endpoint}/${encodeURIComponent(username)}/enabled`, payload);
  }

  resetUserPassword(username: string, payload: AdminResetPasswordPayload): Observable<UserResponse> {
    return this.http.post<UserResponse>(`${this.endpoint}/${encodeURIComponent(username)}/reset-password`, payload);
  }

  deleteUser(username: string): Observable<void> {
    return this.http.delete<void>(`${this.endpoint}/${encodeURIComponent(username)}`);
  }
}
