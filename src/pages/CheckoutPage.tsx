import React, { useState } from 'react';
import { ArrowRight, Lock, ShieldCheck, CreditCard, Trash2, Loader2 } from 'lucide-react';
import { useCart } from '../context/CartContext';
import { createCheckout } from '../services/shopifyService';

export default function CheckoutPage() {
  const { cart, cartTotal, removeFromCart } = useCart();
  const [isRedirecting, setIsRedirecting] = useState(false);
  const tax = cartTotal * 0.08;
  const total = cartTotal + tax;

  const handleCheckout = async () => {
    const shopifyItems = cart
      .filter(item => item.variantId)
      .map(item => ({
        variantId: item.variantId!,
        quantity: item.quantity
      }));

    if (shopifyItems.length > 0) {
      setIsRedirecting(true);
      const checkoutUrl = await createCheckout(shopifyItems);
      if (checkoutUrl) {
        window.location.href = checkoutUrl;
      } else {
        setIsRedirecting(false);
        alert('Failed to connect to Shopify checkout. Please try again.');
      }
    } else {
      alert('This is a demo checkout. Real checkout requires Shopify configuration.');
    }
  };

  return (
    <div className="pt-32 pb-20 px-4 md:px-8 max-w-7xl mx-auto">
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-12">
        {/* Left Column */}
        <div className="lg:col-span-7 space-y-12">
          {/* Contact Information */}
          <section>
            <div className="flex items-center gap-4 mb-8">
              <span className="w-10 h-10 rounded-full bg-primary-container text-on-primary-container flex items-center justify-center font-bold text-lg">1</span>
              <h2 className="text-3xl font-bold tracking-tight">Contact Information</h2>
            </div>
            <div className="space-y-4">
              <div className="space-y-2">
                <label className="text-xs font-bold uppercase tracking-widest text-on-surface-variant ml-1">Email Address</label>
                <input 
                  type="email" 
                  placeholder="alex.rivera@example.com"
                  className="w-full bg-surface-container-high border-none rounded-2xl px-6 py-4 focus:ring-2 focus:ring-primary/40 text-on-surface placeholder:text-on-surface-variant/50 shadow-sm"
                />
              </div>
            </div>
          </section>

          {/* Shipping Address */}
          <section>
            <div className="flex items-center gap-4 mb-8">
              <span className="w-10 h-10 rounded-full bg-primary-container text-on-primary-container flex items-center justify-center font-bold text-lg">2</span>
              <h2 className="text-3xl font-bold tracking-tight">Shipping Address</h2>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="space-y-2">
                <label className="text-xs font-bold uppercase tracking-widest text-on-surface-variant ml-1">First Name</label>
                <input type="text" placeholder="Alex" className="w-full bg-surface-container-high border-none rounded-2xl px-6 py-4 focus:ring-2 focus:ring-primary/40 text-on-surface shadow-sm" />
              </div>
              <div className="space-y-2">
                <label className="text-xs font-bold uppercase tracking-widest text-on-surface-variant ml-1">Last Name</label>
                <input type="text" placeholder="Rivera" className="w-full bg-surface-container-high border-none rounded-2xl px-6 py-4 focus:ring-2 focus:ring-primary/40 text-on-surface shadow-sm" />
              </div>
              <div className="md:col-span-2 space-y-2">
                <label className="text-xs font-bold uppercase tracking-widest text-on-surface-variant ml-1">Street Address</label>
                <input type="text" placeholder="1240 Kinetic Way, Suite 4B" className="w-full bg-surface-container-high border-none rounded-2xl px-6 py-4 focus:ring-2 focus:ring-primary/40 text-on-surface shadow-sm" />
              </div>
              <div className="space-y-2">
                <label className="text-xs font-bold uppercase tracking-widest text-on-surface-variant ml-1">City</label>
                <input type="text" placeholder="San Francisco" className="w-full bg-surface-container-high border-none rounded-2xl px-6 py-4 focus:ring-2 focus:ring-primary/40 text-on-surface shadow-sm" />
              </div>
              <div className="grid grid-cols-2 gap-6">
                <div className="space-y-2">
                  <label className="text-xs font-bold uppercase tracking-widest text-on-surface-variant ml-1">State</label>
                  <input type="text" placeholder="CA" className="w-full bg-surface-container-high border-none rounded-2xl px-6 py-4 focus:ring-2 focus:ring-primary/40 text-on-surface shadow-sm" />
                </div>
                <div className="space-y-2">
                  <label className="text-xs font-bold uppercase tracking-widest text-on-surface-variant ml-1">ZIP</label>
                  <input type="text" placeholder="94103" className="w-full bg-surface-container-high border-none rounded-2xl px-6 py-4 focus:ring-2 focus:ring-primary/40 text-on-surface shadow-sm" />
                </div>
              </div>
            </div>
          </section>

          {/* Payment */}
          <section>
            <div className="flex items-center gap-4 mb-8">
              <span className="w-10 h-10 rounded-full bg-primary-container text-on-primary-container flex items-center justify-center font-bold text-lg">3</span>
              <h2 className="text-3xl font-bold tracking-tight">Payment Method</h2>
            </div>
            <div className="space-y-6">
              <div className="p-8 bg-white rounded-3xl border-2 border-primary/20 flex items-center justify-between shadow-sm">
                <div className="flex items-center gap-6">
                  <CreditCard className="w-10 h-10 text-primary" />
                  <div>
                    <p className="font-bold text-xl">Credit Card</p>
                    <p className="text-sm text-on-surface-variant">Secure encrypted transaction</p>
                  </div>
                </div>
                <div className="flex gap-2">
                  <div className="w-10 h-6 bg-stone-100 rounded border border-stone-200"></div>
                  <div className="w-10 h-6 bg-stone-100 rounded border border-stone-200"></div>
                </div>
              </div>
              <div className="grid grid-cols-1 gap-6 p-8 bg-surface-container-low rounded-3xl">
                <div className="space-y-2">
                  <label className="text-xs font-bold uppercase tracking-widest text-on-surface-variant ml-1">Card Number</label>
                  <div className="relative">
                    <input type="text" placeholder="0000 0000 0000 0000" className="w-full bg-white border-none rounded-2xl px-6 py-4 focus:ring-2 focus:ring-primary/40 text-on-surface shadow-inner" />
                    <Lock className="absolute right-6 top-1/2 -translate-y-1/2 w-5 h-5 text-on-surface-variant/30" />
                  </div>
                </div>
                <div className="grid grid-cols-2 gap-6">
                  <div className="space-y-2">
                    <label className="text-xs font-bold uppercase tracking-widest text-on-surface-variant ml-1">Expiry Date</label>
                    <input type="text" placeholder="MM/YY" className="w-full bg-white border-none rounded-2xl px-6 py-4 focus:ring-2 focus:ring-primary/40 text-on-surface shadow-inner" />
                  </div>
                  <div className="space-y-2">
                    <label className="text-xs font-bold uppercase tracking-widest text-on-surface-variant ml-1">CVC</label>
                    <input type="text" placeholder="123" className="w-full bg-white border-none rounded-2xl px-6 py-4 focus:ring-2 focus:ring-primary/40 text-on-surface shadow-inner" />
                  </div>
                </div>
              </div>
            </div>
          </section>
        </div>

        {/* Right Column: Order Summary */}
        <aside className="lg:col-span-5">
          <div className="sticky top-32 space-y-8">
            <div className="bg-surface-container-low rounded-[3rem] p-10 overflow-hidden relative shadow-sm">
              <div className="absolute -top-12 -right-12 w-48 h-48 bg-primary-container/10 rounded-full blur-3xl"></div>
              <h3 className="text-2xl font-bold mb-10 relative">Order Summary</h3>
              <div className="space-y-8 relative">
                {cart.length === 0 ? (
                  <p className="text-on-surface-variant py-10 text-center italic">Your cart is empty.</p>
                ) : (
                  cart.map((item) => (
                    <div key={item.id} className="flex gap-6 group">
                      <div className="w-24 h-24 bg-white rounded-2xl flex-shrink-0 overflow-hidden shadow-sm">
                        <img src={item.image} alt={item.name} className="w-full h-full object-cover" referrerPolicy="no-referrer" />
                      </div>
                      <div className="flex-grow">
                        <div className="flex justify-between items-start">
                          <div>
                            <p className="font-bold text-lg text-on-surface">{item.name}</p>
                            <p className="text-sm text-on-surface-variant">Qty: {item.quantity}</p>
                          </div>
                          <button 
                            onClick={() => removeFromCart(item.id)}
                            className="text-on-surface-variant hover:text-primary transition-colors p-1"
                          >
                            <Trash2 className="w-4 h-4" />
                          </button>
                        </div>
                        <p className="text-lg font-bold mt-2 text-primary">${(item.price * item.quantity).toFixed(2)}</p>
                      </div>
                    </div>
                  ))
                )}

                <div className="pt-10 space-y-4 border-t border-primary/5">
                  <div className="flex justify-between text-lg">
                    <span className="text-on-surface-variant">Subtotal</span>
                    <span className="font-bold">${cartTotal.toFixed(2)}</span>
                  </div>
                  <div className="flex justify-between text-lg">
                    <span className="text-on-surface-variant">Shipping</span>
                    <span className="text-tertiary font-bold uppercase tracking-widest text-xs">Free</span>
                  </div>
                  <div className="flex justify-between text-lg">
                    <span className="text-on-surface-variant">Estimated Tax</span>
                    <span className="font-bold">${tax.toFixed(2)}</span>
                  </div>
                  <div className="pt-6 flex justify-between items-end">
                    <span className="text-2xl font-bold">Total</span>
                    <div className="text-right">
                      <span className="text-xs text-on-surface-variant block font-bold uppercase tracking-widest">USD</span>
                      <span className="text-5xl font-black tracking-tighter text-on-surface">${total.toFixed(2)}</span>
                    </div>
                  </div>
                </div>
                <button 
                  onClick={handleCheckout}
                  disabled={cart.length === 0 || isRedirecting}
                  className="w-full mt-6 kinetic-gradient text-white font-bold py-6 rounded-full shadow-2xl hover:scale-[1.02] active:scale-[0.98] transition-all flex items-center justify-center gap-3 group disabled:opacity-50 disabled:scale-100"
                >
                  {isRedirecting ? (
                    <Loader2 className="w-6 h-6 animate-spin" />
                  ) : (
                    <>
                      <span>Pay now</span>
                      <ArrowRight className="w-6 h-6 transition-transform group-hover:translate-x-1" />
                    </>
                  )}
                </button>
              </div>
            </div>
            <div className="bg-tertiary-container/20 rounded-[2rem] p-8 flex items-center gap-6 shadow-sm">
              <div className="w-14 h-14 rounded-full bg-white flex items-center justify-center text-tertiary shadow-lg">
                <ShieldCheck className="w-8 h-8" />
              </div>
              <div>
                <p className="text-lg font-bold text-on-tertiary-container">Sipora Guarantee</p>
                <p className="text-sm text-on-tertiary-container/70">30-day hassle-free returns on all kinetic gear.</p>
              </div>
            </div>
          </div>
        </aside>
      </div>
    </div>
  );
}
