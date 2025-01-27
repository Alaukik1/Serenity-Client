import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';
import * as sgMail from '@sendgrid/mail';

admin.initializeApp();
sgMail.setApiKey(functions.config().sendgrid.key);

exports.sendVerificationEmail = functions.auth.user().onCreate(async (user) => {
    const userRecord = await admin.auth().getUser(user.uid);
    const userData = await admin.firestore().collection('users').doc(user.uid).get();
    const fullName = userData.get('fullName');

    const verificationLink = await admin.auth().generateEmailVerificationLink(user.email!);

    const msg = {
        to: user.email!,
        from: 'noreply@serenity.com',
        subject: 'Welcome to Serenity - Verify Your Email',
        html: `
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #FFF1F1;">
                <div style="text-align: center; margin-bottom: 30px;">
                    <h1 style="color: #000;">Serenity</h1>
                    <h2 style="color: #000;">Verify Your Email Address</h2>
                </div>

                <div style="background-color: white; padding: 30px; border-radius: 10px;">
                    <p>Hello ${fullName},</p>
                    
                    <p>Welcome to Serenity! We're excited to have you join our community focused on mental well-being.</p>
                    
                    <p>Please verify your email address to activate your account and begin your journey with us.</p>

                    <div style="text-align: center; margin: 30px 0;">
                        <a href="${verificationLink}" 
                           style="background-color: #000; color: white; padding: 15px 30px; text-decoration: none; border-radius: 25px;">
                            Verify Email Address
                        </a>
                    </div>

                    <p style="color: #666; font-size: 14px;">
                        If the button doesn't work, copy and paste this link into your browser:<br>
                        ${verificationLink}
                    </p>

                    <p style="color: #666;">This verification link will expire in 24 hours.</p>
                    
                    <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                    
                    <p style="color: #666; font-size: 12px;">
                        If you didn't create an account with Serenity, you can safely ignore this email.
                    </p>
                    
                    <div style="text-align: center; margin-top: 30px; color: #666; font-size: 12px;">
                        <p>Need help? Contact us at support@serenity.com</p>
                        <p>© 2024 Serenity by SPK Welfare Foundation. All rights reserved.</p>
                    </div>
                </div>
            </div>
        `
    };

    try {
        await sgMail.send(msg);
        console.log('Verification email sent successfully');
    } catch (error) {
        console.error('Error sending verification email:', error);
    }
}); 