import Client from 'shopify-buy';

// @ts-ignore
const domain = import.meta.env.VITE_SHOPIFY_STORE_DOMAIN;
// @ts-ignore
const storefrontAccessToken = import.meta.env.VITE_SHOPIFY_STOREFRONT_ACCESS_TOKEN;

let client: any = null;

export function getShopifyClient() {
  if (!client) {
    if (!domain || !storefrontAccessToken) {
      console.warn('Shopify domain or storefront access token is missing. Please check your environment variables.');
      return null;
    }
    client = Client.buildClient({
      domain,
      storefrontAccessToken,
      apiVersion: '2024-01'
    });
  }
  return client;
}

export async function fetchAllProducts() {
  const shopify = getShopifyClient();
  if (!shopify) return [];

  try {
    const products = await shopify.product.fetchAll();
    return products.map(p => ({
      id: p.id,
      name: p.title,
      description: p.description,
      price: parseFloat(p.variants[0].price.amount),
      image: p.images[0]?.src || '',
      category: p.productType.toLowerCase().includes('vessel') ? 'vessel' : 'pod',
      colors: p.variants.map(v => v.title), // Simplified color mapping
      tag: p.tags[0] || undefined,
      variantId: p.variants[0].id
    }));
  } catch (error) {
    console.error('Error fetching products from Shopify:', error);
    return [];
  }
}

export async function createCheckout(lineItems: { variantId: string; quantity: number }[]) {
  const shopify = getShopifyClient();
  if (!shopify) return null;

  try {
    const checkout = await shopify.checkout.create();
    await shopify.checkout.addLineItems(checkout.id, lineItems);
    return checkout.webUrl;
  } catch (error) {
    console.error('Error creating Shopify checkout:', error);
    return null;
  }
}
