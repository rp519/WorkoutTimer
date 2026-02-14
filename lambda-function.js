/**
 * AWS Lambda Function for WorkoutTimer App Email Service
 *
 * This function sends workout summary emails via AWS SES (Simple Email Service)
 *
 * SETUP INSTRUCTIONS:
 * 1. Create a Lambda function in AWS Console (Node.js 18.x or later)
 * 2. Set timeout to 30 seconds (Configuration > General > Timeout)
 * 3. Add environment variable: SENDER_EMAIL = "your-verified-email@domain.com"
 * 4. Attach IAM role with SES:SendEmail permission
 * 5. Verify sender email in SES (if in sandbox, verify recipient emails too)
 * 6. Create API Gateway HTTP API endpoint pointing to this Lambda
 * 7. Enable CORS on API Gateway
 */

const AWS = require('aws-sdk');
const ses = new AWS.SES({ region: 'us-east-1' }); // Change region if needed

// Sender email (must be verified in SES)
const SENDER_EMAIL = process.env.SENDER_EMAIL || 'noreply@workoutapp.com';

exports.handler = async (event) => {
    console.log('Received event:', JSON.stringify(event, null, 2));

    try {
        // Parse request body
        let body;
        try {
            body = typeof event.body === 'string' ? JSON.parse(event.body) : event.body;
        } catch (parseError) {
            console.error('Failed to parse request body:', parseError);
            return {
                statusCode: 400,
                headers: getCorsHeaders(),
                body: JSON.stringify({
                    error: 'Invalid JSON in request body',
                    message: parseError.message
                })
            };
        }

        console.log('Parsed body:', JSON.stringify(body, null, 2));

        // Validate required fields
        const { userEmail, subject, html } = body;

        if (!userEmail) {
            return {
                statusCode: 400,
                headers: getCorsHeaders(),
                body: JSON.stringify({ error: 'Missing userEmail field' })
            };
        }

        if (!subject) {
            return {
                statusCode: 400,
                headers: getCorsHeaders(),
                body: JSON.stringify({ error: 'Missing subject field' })
            };
        }

        if (!html) {
            return {
                statusCode: 400,
                headers: getCorsHeaders(),
                body: JSON.stringify({ error: 'Missing html field' })
            };
        }

        // Validate email format
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(userEmail)) {
            return {
                statusCode: 400,
                headers: getCorsHeaders(),
                body: JSON.stringify({ error: 'Invalid email format' })
            };
        }

        console.log(`Sending email to: ${userEmail}`);
        console.log(`Subject: ${subject}`);
        console.log(`HTML length: ${html.length} characters`);

        // Log workout data if present
        if (body.exerciseCategoryBreakdown) {
            console.log('Exercise Category Breakdown:', JSON.stringify(body.exerciseCategoryBreakdown, null, 2));
        }
        if (body.workoutBreakdown) {
            console.log('Workout Breakdown:', JSON.stringify(body.workoutBreakdown, null, 2));
        }

        // Prepare SES email parameters
        const params = {
            Source: SENDER_EMAIL,
            Destination: {
                ToAddresses: [userEmail]
            },
            Message: {
                Subject: {
                    Data: subject,
                    Charset: 'UTF-8'
                },
                Body: {
                    Html: {
                        Data: html,
                        Charset: 'UTF-8'
                    }
                }
            }
        };

        // Send email via SES
        console.log('Calling SES sendEmail...');
        const result = await ses.sendEmail(params).promise();
        console.log('SES sendEmail result:', JSON.stringify(result, null, 2));

        // Return success response
        return {
            statusCode: 200,
            headers: getCorsHeaders(),
            body: JSON.stringify({
                success: true,
                message: 'Email sent successfully',
                messageId: result.MessageId,
                recipient: userEmail,
                workoutData: {
                    monthlyWorkouts: body.monthlyWorkouts || 0,
                    monthlyStreak: body.monthlyStreak || 0,
                    ytdWorkouts: body.ytdWorkouts || 0,
                    exerciseCategories: body.exerciseCategoryBreakdown?.length || 0,
                    workoutTypes: body.workoutBreakdown?.length || 0
                }
            })
        };

    } catch (error) {
        console.error('Error sending email:', error);

        // Handle specific SES errors
        if (error.code === 'MessageRejected') {
            return {
                statusCode: 400,
                headers: getCorsHeaders(),
                body: JSON.stringify({
                    error: 'Email rejected',
                    message: 'The email address may not be verified in SES (sandbox mode)',
                    details: error.message
                })
            };
        }

        if (error.code === 'InvalidParameterValue') {
            return {
                statusCode: 400,
                headers: getCorsHeaders(),
                body: JSON.stringify({
                    error: 'Invalid email parameter',
                    message: error.message
                })
            };
        }

        // Generic error response
        return {
            statusCode: 500,
            headers: getCorsHeaders(),
            body: JSON.stringify({
                error: 'Failed to send email',
                message: error.message,
                code: error.code || 'UNKNOWN_ERROR'
            })
        };
    }
};

/**
 * Get CORS headers for API Gateway response
 */
function getCorsHeaders() {
    return {
        'Content-Type': 'application/json',
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Headers': 'Content-Type',
        'Access-Control-Allow-Methods': 'OPTIONS,POST'
    };
}
