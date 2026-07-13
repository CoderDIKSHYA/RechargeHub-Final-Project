export interface Plan {
  id: number;
  operatorId: number;
  amount: number;
  validity: string;
  description: string;
  data?: string;
  type?: string;
}

export interface Operator {
  id: number;
  name: string;
  type: string;
  circle: string;
  logoUrl?: string;
  plans?: Plan[];
}
