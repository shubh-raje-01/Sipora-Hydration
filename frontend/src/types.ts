export interface Product {
  id: string;
  name: string;
  description: string;
  price: number;
  image: string;
  category: 'vessel' | 'pod';
  colors?: string[];
  specs?: string;
  tag?: string;
  variantId?: string;
}

export interface CartItem extends Product {
  quantity: number;
}
