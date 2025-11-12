# Email Setup Guide for Notification Service

This guide provides step-by-step instructions for setting up free email providers to send notifications from your Spring Boot application.

## Table of Contents
1. [Option 1: MailHog (Easiest - Local Development)](#option-1-mailhog-easiest---local-development)
2. [Option 2: Gmail (Free - Production Ready)](#option-2-gmail-free---production-ready)
3. [Option 3: Outlook/Hotmail (Free - Production Ready)](#option-3-outlookhotmail-free---production-ready)
4. [Option 4: Mailtrap (Free - Testing)](#option-4-mailtrap-free---testing)
5. [Configuration Steps](#configuration-steps)

---

## Option 1: MailHog (Easiest - Local Development)

**Best for:** Local development and testing  
**Cost:** Free  
**Setup Time:** 2 minutes  
**Note:** Emails are captured locally, not actually sent

### Step 1: Start MailHog (Already in Docker Compose)

Your project already has MailHog configured! Just run:

```bash
docker-compose up -d mailhog
```

### Step 2: Verify MailHog is Running

- **SMTP Server:** `localhost:1025`
- **Web UI:** Open http://localhost:8025 in your browser

### Step 3: Update application.properties

Update `notification-service/src/main/resources/application.properties`:

```properties
# Mail Configuration (MailHog - Local Development)
spring.mail.host=localhost
spring.mail.port=1025
spring.mail.username=
spring.mail.password=
spring.mail.properties.mail.smtp.auth=false
spring.mail.properties.mail.smtp.starttls.enable=false
```

### Step 4: Test

1. Start your notification service
2. Trigger an order creation event
3. Check http://localhost:8025 to see the captured email

**✅ Done!** No account creation needed.

---

## Option 2: Gmail (Free - Production Ready)

**Best for:** Production or real email sending  
**Cost:** Free (up to 500 emails/day)  
**Setup Time:** 5-10 minutes

### Step 1: Create or Use Existing Gmail Account

If you don't have a Gmail account:
1. Go to https://accounts.google.com/signup
2. Create a new account

### Step 2: Enable 2-Factor Authentication

1. Go to https://myaccount.google.com/security
2. Click on **2-Step Verification**
3. Follow the prompts to enable it
4. **Important:** You must enable 2FA to generate an App Password

### Step 3: Generate App Password

1. Go to https://myaccount.google.com/apppasswords
   - Or navigate: Google Account → Security → 2-Step Verification → App passwords
2. Select **Mail** as the app
3. Select **Other (Custom name)** as the device
4. Enter a name like "Notification Service"
5. Click **Generate**
6. **Copy the 16-character password** (it will look like: `abcd efgh ijkl mnop`)
   - ⚠️ **Important:** You won't be able to see this password again!

### Step 4: Update application.properties

Update `notification-service/src/main/resources/application.properties`:

```properties
# Mail Configuration (Gmail)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-16-char-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
```

**Replace:**
- `your-email@gmail.com` with your actual Gmail address
- `your-16-char-app-password` with the App Password you generated (remove spaces)

### Step 5: Test

1. Start your notification service
2. Trigger an order creation event
3. Check your Gmail inbox (and spam folder)

**✅ Done!** Your application will now send real emails via Gmail.

---

## Option 3: Outlook/Hotmail (Free - Production Ready)

**Best for:** Production or real email sending  
**Cost:** Free  
**Setup Time:** 5-10 minutes

### Step 1: Create or Use Existing Outlook Account

If you don't have an Outlook account:
1. Go to https://outlook.live.com/
2. Click **Sign up** to create a free account

### Step 2: Enable App Passwords

1. Go to https://account.microsoft.com/security
2. Sign in with your Outlook account
3. Click **Security** → **Advanced security options**
4. Under **App passwords**, click **Create a new app password**
5. Enter a name like "Notification Service"
6. Click **Next**
7. **Copy the generated password** (it will look like: `ABCD-EFGH-IJKL-MNOP`)
   - ⚠️ **Important:** You won't be able to see this password again!

### Step 3: Update application.properties

Update `notification-service/src/main/resources/application.properties`:

```properties
# Mail Configuration (Outlook/Hotmail)
spring.mail.host=smtp-mail.outlook.com
spring.mail.port=587
spring.mail.username=your-email@outlook.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
```

**Replace:**
- `your-email@outlook.com` with your actual Outlook email address
- `your-app-password` with the App Password you generated

### Step 4: Test

1. Start your notification service
2. Trigger an order creation event
3. Check your Outlook inbox (and spam folder)

**✅ Done!** Your application will now send real emails via Outlook.

---

## Option 4: Mailtrap (Free - Testing)

**Best for:** Testing email functionality without sending real emails  
**Cost:** Free (up to 500 emails/month)  
**Setup Time:** 3-5 minutes

### Step 1: Create Mailtrap Account

1. Go to https://mailtrap.io/
2. Click **Sign Up** (free account)
3. Verify your email address

### Step 2: Get SMTP Credentials

1. After logging in, go to **Email Testing** → **Inboxes**
2. Click on your default inbox (or create a new one)
3. Go to **SMTP Settings** tab
4. Select **Integrations** → **Spring Boot**
5. Copy the configuration values

### Step 3: Update application.properties

Update `notification-service/src/main/resources/application.properties`:

```properties
# Mail Configuration (Mailtrap - Testing)
spring.mail.host=sandbox.smtp.mailtrap.io
spring.mail.port=2525
spring.mail.username=your-mailtrap-username
spring.mail.password=your-mailtrap-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

**Replace:**
- `your-mailtrap-username` with your Mailtrap username
- `your-mailtrap-password` with your Mailtrap password

### Step 4: Test

1. Start your notification service
2. Trigger an order creation event
3. Check your Mailtrap inbox to see the captured email

**✅ Done!** Emails are captured in Mailtrap for testing.

---

## Configuration Steps

### For Local Development (Recommended Start)

1. **Use MailHog** (Option 1) - Easiest, no setup needed
2. Update `application.properties` with MailHog settings
3. Start MailHog: `docker-compose up -d mailhog`
4. Start your notification service

### For Production/Real Emails

1. Choose Gmail (Option 2) or Outlook (Option 3)
2. Follow the step-by-step guide for your chosen provider
3. Update `application.properties` with your credentials
4. Test by triggering an order creation event

### Using Environment Variables (Recommended for Production)

Instead of hardcoding credentials in `application.properties`, use environment variables:

```properties
# application.properties
spring.mail.host=${MAIL_HOST:smtp.gmail.com}
spring.mail.port=${MAIL_PORT:587}
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

Then set environment variables:
- Windows PowerShell: `$env:MAIL_USERNAME="your-email@gmail.com"`
- Windows CMD: `set MAIL_USERNAME=your-email@gmail.com`
- Linux/Mac: `export MAIL_USERNAME="your-email@gmail.com"`

### Using Config Server

If you're using Spring Cloud Config Server, you can store these settings in:
`configserver/src/main/resources/config/notification-service.properties`

---

## Troubleshooting

### Issue: "Connection refused" or "Authentication failed"

**Solutions:**
1. **Gmail:** Make sure you're using an App Password, not your regular password
2. **Outlook:** Make sure you generated an App Password
3. **Check firewall:** Ensure port 587 is not blocked
4. **Verify credentials:** Double-check username and password

### Issue: "Could not connect to SMTP host"

**Solutions:**
1. Check your internet connection
2. Verify the SMTP host and port are correct
3. Check if your ISP blocks port 587 (try port 465 with SSL instead)

### Issue: Emails going to spam

**Solutions:**
1. Add SPF/DKIM records (for production)
2. Use a proper "From" address
3. Include unsubscribe links
4. Avoid spam trigger words in subject/content

### Issue: "Too many emails" error

**Solutions:**
1. Gmail free tier: 500 emails/day limit
2. Use MailHog or Mailtrap for testing
3. Consider upgrading to a paid email service for production

---

## Quick Reference: SMTP Settings

| Provider | Host | Port | Auth | TLS |
|----------|------|------|------|-----|
| **MailHog** | localhost | 1025 | No | No |
| **Gmail** | smtp.gmail.com | 587 | Yes | Yes |
| **Outlook** | smtp-mail.outlook.com | 587 | Yes | Yes |
| **Mailtrap** | sandbox.smtp.mailtrap.io | 2525 | Yes | Yes |

---

## Next Steps

1. **For Development:** Start with MailHog (Option 1)
2. **For Testing:** Use Mailtrap (Option 4)
3. **For Production:** Use Gmail (Option 2) or Outlook (Option 3)
4. **For Scale:** Consider SendGrid, Mailgun, or AWS SES (paid services)

---

## Security Best Practices

1. ✅ **Never commit credentials to Git** - Use environment variables or config server
2. ✅ **Use App Passwords** - Don't use your main account password
3. ✅ **Rotate passwords regularly** - Especially for production
4. ✅ **Use environment-specific configs** - Different settings for dev/staging/prod
5. ✅ **Monitor email usage** - Watch for unusual activity

---

**Need Help?** Check the Spring Boot Mail documentation: https://docs.spring.io/spring-boot/docs/current/reference/html/io.html#io.email








