export interface User {
  id: number;
  name: string;
  email: string;
  role: string;
  phoneNumber: string;
  createdAt?: string;
  profilePictureUrl?: string;
}

export interface AuthResponse {
  token: string;
  user: User;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  phoneNumber: string;
}
