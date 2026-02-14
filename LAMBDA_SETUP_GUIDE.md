# AWS Lambda Email Service - Setup Guide

This guide walks you through setting up the email service for the WorkoutTimer Android app using AWS Lambda and SES.

## Prerequisites

- AWS Account
- Access to AWS Console
- Email address for sending emails (must be verified in SES)

---

## Step 1: Verify Email in Amazon SES

### If in SES Sandbox Mode (default for new accounts):
1. Go to **Amazon SES Console** → **Verified identities**
2. Click **Create identity**
3. Select **Email address**
4. Enter your sender email (e.g., `noreply@yourdomain.com`)
5. Click **Create identity**
6. Check your email and click the verification link
7. **IMPORTANT**: In sandbox mode, you must also verify all recipient emails
   - Repeat steps 2-6 for each user email that will receive workout summaries
   - OR request production access (see Step 6)

---

## Step 2: Create IAM Role for Lambda

1. Go to **IAM Console** → **Roles** → **Create role**
2. Select **AWS service** → **Lambda**
3. Click **Next**
4. Attach these policies:
   - `AWSLambdaBasicExecutionRole` (for CloudWatch logs)
   - `AmazonSESFullAccess` (for sending emails) - OR create custom policy below
5. Click **Next**
6. Name the role: `WorkoutTimerEmailLambdaRole`
7. Click **Create role**

### Custom SES Policy (More Secure):
Instead of full access, use this minimal policy:
```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "ses:SendEmail",
                "ses:SendRawEmail"
            ],
            "Resource": "*"
        }
    ]
}
```

---

## Step 3: Create Lambda Function

1. Go to **Lambda Console** → **Create function**
2. Select **Author from scratch**
3. Configuration:
   - **Function name**: `WorkoutTimerEmailService`
   - **Runtime**: Node.js 18.x (or latest)
   - **Architecture**: x86_64
   - **Permissions**: Use existing role → Select `WorkoutTimerEmailLambdaRole`
4. Click **Create function**

---

## Step 4: Configure Lambda Function

### 4.1 Upload Function Code
1. In the Lambda function page, scroll to **Code source**
2. Delete the default `index.js` content
3. Copy and paste the contents of `lambda-function.js` (from this repository)
4. Click **Deploy**

### 4.2 Set Environment Variables
1. Go to **Configuration** tab → **Environment variables**
2. Click **Edit** → **Add environment variable**
3. Add:
   - **Key**: `SENDER_EMAIL`
   - **Value**: Your verified email (e.g., `noreply@yourdomain.com`)
4. Click **Save**

### 4.3 Adjust Timeout
1. Go to **Configuration** tab → **General configuration**
2. Click **Edit**
3. Set **Timeout** to **30 seconds**
4. Click **Save**

### 4.4 Test the Function (Optional)
1. Go to **Test** tab
2. Create new test event:
```json
{
  "body": "{\"userEmail\":\"your-test-email@example.com\",\"subject\":\"Test Email\",\"html\":\"<h1>Test</h1><p>This is a test email from WorkoutTimer</p>\",\"monthlyWorkouts\":5,\"monthlyStreak\":3}"
}
```
3. Click **Test**
4. Check the execution results (should see "Email sent successfully")
5. Check your email inbox

---

## Step 5: Create API Gateway

### 5.1 Create HTTP API
1. Go to **API Gateway Console** → **Create API**
2. Select **HTTP API** → **Build**
3. Click **Add integration** → **Lambda**
4. Select your Lambda function: `WorkoutTimerEmailService`
5. API name: `WorkoutTimerEmailAPI`
6. Click **Next**

### 5.2 Configure Routes
1. Method: **POST**
2. Resource path: `/send-email`
3. Integration target: Your Lambda function
4. Click **Next**

### 5.3 Configure CORS
1. On the CORS configuration page:
   - **Access-Control-Allow-Origin**: `*`
   - **Access-Control-Allow-Headers**: `Content-Type`
   - **Access-Control-Allow-Methods**: `POST, OPTIONS`
2. Click **Next**

### 5.4 Review and Create
1. Review the configuration
2. Click **Create**
3. **COPY THE INVOKE URL** - you'll need this for the Android app

Example URL: `https://abc123xyz.execute-api.us-east-1.amazonaws.com/prod/send-email`

---

## Step 6: Request SES Production Access (Recommended)

By default, SES is in **sandbox mode** which requires verifying every recipient email. To send to any email address:

1. Go to **Amazon SES Console** → **Account dashboard**
2. Click **Request production access**
3. Fill out the form:
   - **Mail type**: Transactional
   - **Website URL**: Your app's website or Play Store link
   - **Use case description**:
     ```
     We are sending personalized workout summary emails to users of our fitness mobile app.
     Users opt-in to receive weekly/monthly progress reports showing their workout statistics,
     streaks, and achievements. Emails are sent only to verified users who have enabled
     email summaries in app settings.
     ```
   - **Compliance**: Confirm you comply with AWS policies
4. Submit the request
5. Wait 24-48 hours for AWS review

---

## Step 7: Update Android App

1. Open `EmailService.kt` in the Android project
2. Update the `API_ENDPOINT` constant with your API Gateway URL:
```kotlin
private const val API_ENDPOINT = "https://YOUR-API-ID.execute-api.us-east-1.amazonaws.com/prod/send-email"
```
3. Build and deploy the updated app

---

## Testing the Integration

### Test 1: Send Test Email from App
1. Open the WorkoutTimer app
2. Complete at least one workout (to generate data)
3. Go to **Settings**
4. Set your email address
5. Enable **Workout Summary Emails**
6. Tap **Send Test Email**
7. Check your email inbox (may take 1-2 minutes)

### Test 2: Check CloudWatch Logs
1. Go to **Lambda Console** → Your function → **Monitor** tab
2. Click **View CloudWatch logs**
3. Check the latest log stream for:
   - Incoming request data
   - Exercise category breakdown
   - Workout breakdown
   - SES response

---

## Troubleshooting

### Error: "Email rejected"
- **Cause**: Email address not verified in SES (sandbox mode)
- **Solution**: Verify the recipient email in SES Console

### Error: "Failed to send email"
- **Cause**: Lambda doesn't have SES permissions
- **Solution**: Check IAM role has SES:SendEmail permission

### Error: "Invalid email format"
- **Cause**: Email address doesn't match regex pattern
- **Solution**: Ensure email is valid format (e.g., `user@domain.com`)

### Error: Timeout
- **Cause**: Lambda timeout is too short
- **Solution**: Increase timeout to 30 seconds in Lambda configuration

### No email received
1. Check CloudWatch logs for errors
2. Verify sender email in SES
3. Check spam folder
4. Ensure API Gateway endpoint is correct in Android app

---

## Cost Estimation

### AWS Free Tier (First 12 months):
- **Lambda**: 1M requests/month free
- **SES**: 62,000 emails/month free (when sending from EC2 or Lambda)
- **API Gateway**: 1M requests/month free

### After Free Tier:
- **Lambda**: $0.20 per 1M requests + $0.0000166667 per GB-second
- **SES**: $0.10 per 1,000 emails
- **API Gateway**: $1.00 per million requests

**Example**: 10,000 users, weekly emails = 40,000 emails/month = **~$0.40/month**

---

## Security Best Practices

1. **Use Custom Domain**: Set up domain verification in SES for better deliverability
2. **Rate Limiting**: Add throttling in API Gateway to prevent abuse
3. **Authentication**: Consider adding API keys or JWT tokens for production
4. **Monitoring**: Set up CloudWatch alarms for failed emails
5. **Email Validation**: The Lambda function validates email format before sending
6. **Error Logging**: All errors are logged to CloudWatch for debugging

---

## Email Data Schema

The Android app sends this JSON payload to the Lambda function:

```json
{
  "userEmail": "user@example.com",
  "subject": "Your Workout Summary",
  "html": "<html>...</html>",
  "userName": "John Doe",
  "currentMonth": "January 2026",
  "monthlyWorkouts": 12,
  "monthlyRounds": 48,
  "monthlyDuration": "2h 30m",
  "monthlyStreak": 7,
  "ytdWorkouts": 45,
  "ytdRounds": 180,
  "ytdDuration": "10h 15m",
  "ytdActiveDays": 35,
  "mostUsedWorkout": "Full Body Blast",
  "exerciseCategoryBreakdown": [
    {
      "category": "abs",
      "exerciseCount": 15,
      "sessionCount": 8
    },
    {
      "category": "chest",
      "exerciseCount": 10,
      "sessionCount": 6
    }
  ],
  "workoutBreakdown": [
    {
      "planName": "Full Body Blast",
      "count": 5,
      "totalSeconds": 1800,
      "duration": "30m"
    }
  ]
}
```

---

## Support

For issues or questions:
1. Check CloudWatch logs in AWS Console
2. Review this guide's troubleshooting section
3. Verify all steps were completed correctly

---

## Next Steps

After successful setup:
1. ✅ Test email sending from the app
2. ✅ Request SES production access
3. ✅ Set up custom domain for better email deliverability
4. ✅ Monitor CloudWatch metrics
5. ✅ Consider adding unsubscribe functionality (required for production)
