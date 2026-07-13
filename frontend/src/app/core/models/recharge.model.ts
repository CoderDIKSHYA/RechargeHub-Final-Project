export interface RechargeRequest {
  operatorId: number;
  planId: number;
  mobileNumber: string;
  paymentMethod: string;
}

export interface RechargeResponse {
  id: number;
  userId: number;
  operatorId: number;
  planId: number;
  mobileNumber: string;
  amount: number;
  status: string;
  createdAt: string;
  message: string;
  transactionId?: string;
}

export interface NotificationResponse {
  id: number;
  userId: number;
  message: string;
  type: string;
  status: string;
  createdAt: string;
}
