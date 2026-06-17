# Razorpay Payment Setup Guide - AI Prompt Generator

---

## Step 1: Razorpay Account Create Karo

1. [Razorpay Website](https://razorpay.com/) par jao aur **"Sign Up"** par click karo.
2. Apne mobile number/email address aur basic detail se register karo.
3. Apni Business details bharo (Business type, PAN number, Bank account details jisme payment receive karna hai).
4. KYC documents upload karo (Aadhaar, PAN, Bank Proof, etc.) aur account validation complete hone ka wait karo (usually 24-48 hours lagte hain activation me).

> [!NOTE]
> Active validation complete hone tak aap **Test Mode (Sandbox)** ka use karke payment test kar sakte hain.

---

## Step 2: API Keys Generate Karo

### Test Mode (Sandbox) - For Testing

1. Razorpay Dashboard par login karke check karein ki top right me toggle **"Test Mode"** par ho.
2. **Account & Settings** > **API Keys** Section me jao.
3. **"Generate Key"** par click karo.
4. Aapko ye API Keys milengi:
   - **Key ID:** `rzp_test_xxxxxxxxxxxxxx`
   - **Key Secret:** `xxxxxxxxxxxxxxxxxxxxxxxx`
5. In dono credentials ko copy karke save kar lein.

### Live Mode (Production) - For Real Payments

1. Jab KYC approve ho jaye, to top right toggle se **"Live Mode"** select karein.
2. **Account & Settings** > **API Keys** par jao aur **"Generate Key"** par click karo.
3. Aapko ye keys milengi:
   - **Key ID:** `rzp_live_xxxxxxxxxxxxxx`
   - **Key Secret:** (live secret)
4. In credentials ko save kar lein.

---

## Step 3: Admin Panel me keys update karein (Dynamic Configurations)

Ab aapko app me keys hardcode karne ki zaroorat nahi hai. Aap asani se Admin Panel me in keys ko add kar sakte hain:

1. Project ke Admin Panel website par Login karein.
2. Sidebar se **Pricing Tier Manager** (या System Settings) page par jayein.
3. **Razorpay Gateway Configurations** section me:
   - **Razorpay Key ID (Sandbox / Test Mode):** me apni test key ID (`rzp_test_...`) paste karein.
   - **Razorpay Key ID (Production / Live Mode):** me apni live key ID (`rzp_live_...`) paste karein.
4. **"Save Settings"** button par click karein. Ye values automatic Firebase Firestore ke `config/system` document me sync ho jayengi.

---

## Step 4: Test Payments (Test Mode)

App me test payments karne ke liye niche diye gaye credentials ka use karein:

### Test Cards (UPI / Netbanking aur Cards test karne ke liye)

| Card Number | Expiry | CVV | Card Holder Name | Result |
|-------------|--------|-----|------------------|--------|
| `4111 1111 1111 1111` | Any future date | `123` | Any Name | Success (Successful Payment) |
| `4111 1111 1111 1140` | Any future date | `123` | Any Name | Fail (Payment Failed) |

### Test UPI Payments

1. Payment option me **UPI** option select karein.
2. **UPI ID** me `success@razorpay` enter karein.
3. Verify karne ke baad proceed karein. Payment absolute mock framework ke dwara process hokar success show karegi.

---

## Step 5: Android App me switch karna Sandbox / Live mode

Android application me:
1. **Wallet** (Credit Store) screen par jayein.
2. **Sandbox Mock Mode** ka toggle active hone par Test API Key ID use hoga aur transaction mock test environment me hoga.
3. Toggle OFF karne par Live/Production API key set hogi aur real payments ke liye request execute hogi.

---

## Go Live Checklist

- [ ] Razorpay KYC approved & account activated.
- [ ] Sandbox/Test Key ID `rzp_test_...` admin panel config me saved.
- [ ] Live/Production Key ID `rzp_live_...` admin panel config me saved.
- [ ] App me Sandbox Mode toggle switch OFF karke payment verify.
- [ ] Real ₹1 transaction karke testing checkout complete checked.
