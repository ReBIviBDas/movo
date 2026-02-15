/**
 * Email Service
 * Mock implementation for development - logs emails to console
 */

const sendEmail = async ({ to, subject, body, html }) => {
    console.log('\n📧 ═══════════════════════════════════════════════════');
    console.log('📧 MOCK EMAIL SERVICE - Email would be sent:');
    console.log('📧 ═══════════════════════════════════════════════════');
    console.log(`📧 To: ${to}`);
    console.log(`📧 Subject: ${subject}`);
    console.log(`📧 Body: ${body || '(HTML content)'}`);
    if (html) {
        console.log(`📧 HTML: ${html.substring(0, 100)}...`);
    }
    console.log('📧 ═══════════════════════════════════════════════════\n');
    
    return Promise.resolve({ success: true, messageId: `mock-${Date.now()}` });
};

const sendRegistrationConfirmation = async (user) => {
    return sendEmail({
        to: user.email,
        subject: 'Benvenuto in MOVO - Conferma registrazione',
        body: `Ciao ${user.first_name},\n\nGrazie per esserti registrato a MOVO!\n\n` +
              `Il tuo account è stato creato con successo.\n` +
              (user.status === 'pending_approval' 
                  ? 'I tuoi documenti sono in fase di verifica. Ti notificheremo appena approvati.\n'
                  : 'Puoi iniziare a usare il servizio immediatamente.\n') +
              `\nA presto,\nIl team MOVO`
    });
};

const sendApprovalNotification = async (user, approved, reason = '') => {
    const subject = approved 
        ? 'MOVO - Account approvato!' 
        : 'MOVO - Documenti non approvati';
    
    const body = approved
        ? `Ciao ${user.first_name},\n\nOttima notizia! I tuoi documenti sono stati verificati e approvati.\n` +
          `Ora puoi prenotare e noleggiare i veicoli MOVO.\n\nA presto,\nIl team MOVO`
        : `Ciao ${user.first_name},\n\nPurtroppo i tuoi documenti non sono stati approvati.\n` +
          `Motivo: ${reason || 'Documenti non leggibili o non validi'}\n` +
          `Ti invitiamo a caricare nuovamente i documenti.\n\nA presto,\nIl team MOVO`;
    
    return sendEmail({ to: user.email, subject, body });
};

module.exports = {
    sendEmail,
    sendRegistrationConfirmation,
    sendApprovalNotification
};
