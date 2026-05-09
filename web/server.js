const express = require('express');
const session = require('express-session');
const multer = require('multer');
const path = require('path');
const fs = require('fs');
const { initDB, all, get, run } = require('./db');
const cloudinary = require('cloudinary').v2;

cloudinary.config({
  cloud_name: 'dpgjiionz',
  api_key: '385438693415915',
  api_secret: '3DRKdVWCABj9yU_d9ciQJ7xmYtg'
});

let admin = null;
const firebaseAdmin = require('firebase-admin');
let serviceAccount;
try {
  const dirs = [__dirname, '/etc/secrets'];
  let serviceAccountFile = null;
  for (const dir of dirs) {
    if (fs.existsSync(dir)) {
      const files = fs.readdirSync(dir);
      const found = files.find(f => f.includes('firebase-adminsdk') && f.endsWith('.json'));
      if (found) {
        serviceAccountFile = path.join(dir, found);
        break;
      }
    }
  }

  if (serviceAccountFile) {
    serviceAccount = JSON.parse(fs.readFileSync(serviceAccountFile, 'utf8'));
    if (firebaseAdmin.apps.length === 0) {
      admin = firebaseAdmin.initializeApp({
        credential: firebaseAdmin.credential.cert(serviceAccount)
      });
      console.log('[FCM] ✅ Firebase Admin initialized from:', serviceAccountFile);
    } else {
      admin = firebaseAdmin.app();
      console.log('[FCM] ✅ Firebase Admin already initialized');
    }
  } else {
    console.log('[FCM] ⚠️ Firebase Admin init skipped: No firebase-adminsdk JSON found in', dirs);
  }
} catch (error) {
  console.error('[FCM] ❌ Error initializing Firebase Admin:', error);
}

const app = express();
const PORT = process.env.PORT || 3000;

['uploads/notes','uploads/qr'].forEach(d => { const p = path.join(__dirname, d); if (!fs.existsSync(p)) fs.mkdirSync(p, { recursive: true }); });

app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(express.static(path.join(__dirname, 'public')));
app.use('/uploads', express.static(path.join(__dirname, 'uploads')));
app.use(session({ secret: process.env.SESSION_SECRET || 'shreeji-secret-2026', resave: false, saveUninitialized: false, cookie: { maxAge: 30*24*60*60*1000 } }));

const noteUpload = multer({ storage: multer.diskStorage({ destination: (r,f,cb) => cb(null, path.join(__dirname,'uploads/notes')), filename: (r,f,cb) => cb(null, Date.now()+'-'+f.originalname.replace(/\s/g,'_')) }), limits: { fileSize: 100*1024*1024 } });
const qrUpload = multer({ storage: multer.diskStorage({ destination: (r,f,cb) => cb(null, path.join(__dirname,'uploads/qr')), filename: (r,f,cb) => cb(null, 'qr-'+Date.now()+path.extname(f.originalname)) }), limits: { fileSize: 5*1024*1024 } });

function auth(req, res, next) { if (!req.session.user) return res.status(401).json({ error: 'Not logged in' }); next(); }
function teacherOnly(req, res, next) { if (req.session.user?.role !== 'TEACHER') return res.status(403).json({ error: 'Teacher only' }); next(); }
function adminOnly(req, res, next) { if (req.session.user?.role !== 'ADMIN') return res.status(403).json({ error: 'Admin only' }); next(); }

// Helper: create notification
async function notify(userId, title, message, type) {
  const uid = userId.toLowerCase().trim();
  await run('INSERT INTO notifications("userId",title,message,type,"createdAt") VALUES($1,$2,$3,$4,$5)', [uid, title, message, type||'INFO', Date.now()]);
  
  if (admin) {
    try {
      const u = await get('SELECT "fcmToken" FROM users WHERE LOWER("userId")=$1', [uid]);
      if (u && u.fcmToken && u.fcmToken.length > 10) {
        console.log(`[FCM] Sending to ${uid}: ${title}`);
        await admin.messaging().send({
          token: u.fcmToken,
          notification: {
            title: String(title),
            body: String(message)
          },
          data: { 
            type: String(type || 'INFO') 
          },
          android: {
            priority: 'high',
            notification: {
              channelId: 'shreeji_fcm',
              defaultSound: true,
              defaultVibrateTimings: true
            }
          }
        });
        console.log(`[FCM] Success for ${uid}`);
      } else {
        console.log(`[FCM] No token for ${uid}`);
      }
    } catch(e) {
      console.log(`[FCM] Error for ${uid}:`, e.message);
      if (e.code === 'messaging/registration-token-not-registered') {
        await run('UPDATE users SET "fcmToken"=\'\' WHERE LOWER("userId")=$1', [uid]);
      }
    }
  } else {
    console.log('[FCM] Admin not initialized');
  }
}

app.get('/api/debug/fcm-status/:userId', async (req, res) => {
  const uid = req.params.userId.toLowerCase().trim();
  try {
    const u = await get('SELECT "fcmToken" FROM users WHERE LOWER("userId")=$1', [uid]);
    res.json({
      firebaseAdminInitialized: !!admin,
      userExists: !!u,
      tokenSaved: u ? (u.fcmToken && u.fcmToken.length > 10) : false,
      tokenPreview: u && u.fcmToken ? u.fcmToken.substring(0, 15) + '...' : null,
      tokenLength: u && u.fcmToken ? u.fcmToken.length : 0
    });
  } catch (e) {
    res.status(500).json({ error: e.message });
  }
});
async function notifyStudents(targetId, title, message, type) {
  const target = targetId.toLowerCase().trim();
  if (target === 'all') {
    const students = await all("SELECT \"userId\" FROM users WHERE role='STUDENT'");
    for (const s of students) await notify(s.userId, title, message, type);
  } else {
    await notify(target, title, message, type);
  }
}

// AUTH
app.post('/api/login', async (req, res) => {
  const { userId, password } = req.body;
  const uid = userId.toLowerCase().trim();
  console.log(`[Login Attempt] ID: ${uid}`);
  const user = await get('SELECT * FROM users WHERE LOWER("userId")=$1 AND password=$2', [uid, password]);
  if (!user) {
    console.log(`[Login Failed] Invalid credentials for: ${uid}`);
    return res.status(401).json({ error: 'Invalid credentials' });
  }
  req.session.user = { userId: user.userId, role: user.role, name: user.name };
  console.log(`[Login Success] ${user.name} (${user.role})`);
  
  if (user.role === 'STUDENT') {
    const now = new Date(), curM = now.getMonth()+1, curY = now.getFullYear();
    const overdue = await all('SELECT * FROM fees WHERE "studentId"=$1 AND status=$2 AND (year<$3 OR (year=$3 AND month<$4))', [user.userId, 'UNPAID', curY, curM]);
    if (overdue.length > 0) {
      const existing = await get('SELECT * FROM notifications WHERE "userId"=$1 AND type=$2 AND "createdAt">$3', [user.userId, 'FEE_REMINDER', Date.now()-86400000]);
      if (!existing) await notify(user.userId, '💰 Fee Due Reminder', `You have ${overdue.length} overdue fee(s). Please pay immediately.`, 'FEE_REMINDER');
    }
  }
  res.json({ user: req.session.user });
});

// NOTIFICATIONS
app.get('/api/notifications', auth, async (req, res) => {
  res.json(await all('SELECT * FROM notifications WHERE LOWER("userId")=LOWER($1) ORDER BY "createdAt" DESC LIMIT 50', [req.session.user.userId]));
});
app.get('/api/notifications/unread-count', auth, async (req, res) => {
  const c = await get('SELECT COUNT(*) as c FROM notifications WHERE LOWER("userId")=LOWER($1) AND read=false', [req.session.user.userId]);
  res.json({ count: parseInt(c?.c || 0) });
});
app.put('/api/notifications/read-all', auth, async (req, res) => {
  await run('UPDATE notifications SET read=true WHERE LOWER("userId")=LOWER($1)', [req.session.user.userId]);
  res.json({ ok: true });
});
app.post('/api/notifications/clear', auth, async (req, res) => {
  await run('DELETE FROM notifications WHERE LOWER("userId")=LOWER($1)', [req.session.user.userId]);
  res.json({ ok: true });
});

// SESSION
app.post('/api/logout', (req, res) => { req.session.destroy(); res.json({ ok: true }); });
app.get('/api/session', (req, res) => { res.json({ user: req.session.user || null }); });
app.post('/api/change-password', auth, async (req, res) => {
  const { oldPassword, newPassword } = req.body;
  const u = await get('SELECT * FROM users WHERE "userId"=$1 AND password=$2', [req.session.user.userId, oldPassword]);
  if (!u) return res.status(400).json({ error: 'Wrong current password' });
  await run('UPDATE users SET password=$1 WHERE "userId"=$2', [newPassword, req.session.user.userId]);
  res.json({ ok: true });
});
app.post('/api/fcm-token', async (req, res) => {
  const { token, userId } = req.body;
  const uid = (userId || req.session.user?.userId || '').toLowerCase().trim();
  
  if (token && uid) {
    await run('UPDATE users SET "fcmToken"=$1 WHERE LOWER("userId")=$2', [token, uid]);
    console.log(`[FCM] ✅ Token registered for ${uid}: ${token.slice(0, 10)}...`);
    return res.json({ ok: true, registered: uid });
  }
  
  console.log(`[FCM] ❌ Registration failed. Token: ${!!token}, UID: ${uid}`);
  res.status(400).json({ error: 'Missing token or userId' });
});

// Debug endpoint to test notification delivery
app.get('/api/debug/notify/:uid', async (req, res) => {
  const { uid } = req.params;
  await notify(uid, '🔔 Test Notification', 'This is a manual test from the server.', 'TEST');
  res.json({ ok: true, message: `Attempted to notify ${uid}` });
});


// STUDENTS
app.get('/api/students', auth, teacherOnly, async (req, res) => { res.json(await all('SELECT * FROM users WHERE role=$1 ORDER BY name', ['STUDENT'])); });
app.post('/api/students', auth, teacherOnly, async (req, res) => {
  const { name, studentClass, phone, parentPhone, monthlyFee, userId, password, startMonth, startYear, durationMonths } = req.body;
  const id = (userId || 'STU' + (100 + Math.floor(Math.random()*900))).toLowerCase().trim();
  const pwd = password || Math.random().toString(36).slice(-6);
  const exists = await get('SELECT "userId" FROM users WHERE LOWER("userId")=$1', [id]);
  if (exists) return res.status(400).json({ error: 'Student ID already exists' });
  await run('INSERT INTO users("userId",name,role,password,phone,"parentPhone","studentClass","monthlyFee","createdAt") VALUES($1,$2,$3,$4,$5,$6,$7,$8,$9)', [id, name, 'STUDENT', pwd, phone||'', parentPhone||'', studentClass||'', monthlyFee||0, Date.now()]);
  if (monthlyFee && monthlyFee > 0 && durationMonths && durationMonths > 0) {
    let m = parseInt(startMonth) || (new Date().getMonth()+1);
    let y = parseInt(startYear) || new Date().getFullYear();
    for (let i = 0; i < parseInt(durationMonths); i++) {
      const dueDate = new Date(y, m-1, 10).getTime();
      await run('INSERT INTO fees("studentId",month,year,amount,status,"dueDate") VALUES($1,$2,$3,$4,$5,$6)', [id, m, y, monthlyFee, 'UNPAID', dueDate]);
      m++; if (m > 12) { m = 1; y++; }
    }
  }
  res.json({ userId: id, password: pwd });
});
app.put('/api/students/:id', auth, teacherOnly, async (req, res) => {
  const { name, studentClass, phone, parentPhone, monthlyFee } = req.body;
  await run('UPDATE users SET name=$1,"studentClass"=$2,phone=$3,"parentPhone"=$4,"monthlyFee"=$5 WHERE "userId"=$6', [name, studentClass, phone||'', parentPhone||'', monthlyFee||0, req.params.id]);
  res.json({ ok: true });
});
app.delete('/api/students/:id', auth, teacherOnly, async (req, res) => {
  const uid = req.params.id.toLowerCase();
  await run('DELETE FROM fees WHERE "studentId"=$1', [uid]);
  await run('DELETE FROM doubts WHERE "studentId"=$1', [uid]);
  await run('DELETE FROM results WHERE "studentId"=$1', [uid]);
  await run('DELETE FROM notifications WHERE "userId"=$1', [uid]);
  await run('DELETE FROM users WHERE LOWER("userId")=$1 AND role=$2', [uid, 'STUDENT']);
  res.json({ ok: true });
});

// FEES
app.get('/api/fees', auth, async (req, res) => {
  const q = req.query.studentId;
  res.json(q ? await all('SELECT f.*,u.name as "studentName" FROM fees f LEFT JOIN users u ON f."studentId"=u."userId" WHERE LOWER(f."studentId")=$1 ORDER BY f.year DESC,f.month DESC', [q.toLowerCase()])
    : await all('SELECT f.*,u.name as "studentName" FROM fees f LEFT JOIN users u ON f."studentId"=u."userId" ORDER BY f.year DESC,f.month DESC'));
});
app.post('/api/fees', auth, teacherOnly, async (req, res) => {
  const { studentId, month, year, amount } = req.body;
  await run('INSERT INTO fees("studentId",month,year,amount,status,"dueDate") VALUES($1,$2,$3,$4,$5,$6)', [studentId.toLowerCase(), month, year, amount, 'UNPAID', Date.now()+30*86400000]);
  res.json({ ok: true });
});
app.put('/api/fees/:id/pay', auth, teacherOnly, async (req, res) => {
  const fee = await get('SELECT * FROM fees WHERE "feeId"=$1', [parseInt(req.params.id)]);
  if (!fee) return res.status(404).json({ error: 'Not found' });
  await run('UPDATE fees SET status=$1,"paidAmount"=$2,"paidDate"=$3 WHERE "feeId"=$4', ['PAID', fee.amount, Date.now(), parseInt(req.params.id)]);
  await notify(fee.studentId, '✅ Fee Paid', `Your fee for month ${fee.month}/${fee.year} (₹${fee.amount}) has been marked as PAID.`, 'FEE_PAID');
  res.json({ ok: true });
});
app.put('/api/fees/:id/unpay', auth, teacherOnly, async (req, res) => {
  await run('UPDATE fees SET status=$1,"paidAmount"=0,"paidDate"=NULL WHERE "feeId"=$2', ['UNPAID', parseInt(req.params.id)]);
  res.json({ ok: true });
});
app.delete('/api/fees/:id', auth, teacherOnly, async (req, res) => {
  await run('DELETE FROM fees WHERE "feeId"=$1', [parseInt(req.params.id)]);
  res.json({ ok: true });
});

// NOTES
app.get('/api/notes', auth, async (req, res) => {
  const u = req.session.user;
  res.json(u.role === 'TEACHER' ? await all('SELECT * FROM notes ORDER BY "uploadDate" DESC')
    : await all("SELECT * FROM notes WHERE \"targetStudentId\"='ALL' OR LOWER(\"targetStudentId\")=$1 ORDER BY \"uploadDate\" DESC", [u.userId.toLowerCase()]));
});
app.post('/api/notes', auth, teacherOnly, noteUpload.single('file'), async (req, res) => {
  const { title, subject, topic, type, content, description, targetStudentId } = req.body;
  let filePath = '';
  
  if (req.file) {
    try {
      console.log(`[Cloudinary] Uploading ${req.file.originalname}...`);
      const isVideo = req.file.mimetype.startsWith('video/') || req.file.originalname.toLowerCase().match(/\\.(mp4|mkv|mov|avi|webm)$/);
      const isRaw = req.file.mimetype === 'application/pdf' || req.file.originalname.toLowerCase().match(/\\.(pdf|doc|docx)$/);
      
      let resType = 'auto';
      if (isVideo) resType = 'video';
      else if (isRaw) resType = 'raw';

      // Use upload_large for videos to prevent timeout/size limit issues
      const uploadMethod = isVideo ? cloudinary.uploader.upload_large : cloudinary.uploader.upload;
      
      const result = await uploadMethod(req.file.path, {
        resource_type: resType,
        folder: 'shreeji_notes'
      });
      filePath = result.secure_url;
      // Clean up local file after upload
      fs.unlinkSync(req.file.path);
      console.log(`[Cloudinary] Upload success: ${filePath}`);
    } catch (e) {
      console.log('[Cloudinary] Upload error:', e.message);
      filePath = '/uploads/notes/' + req.file.filename; // Fallback to local
    }
  }

  const target = (targetStudentId && targetStudentId.toUpperCase() !== 'ALL') ? targetStudentId.toLowerCase() : 'ALL';
  await run('INSERT INTO notes(title,subject,topic,type,"filePath",content,description,"targetStudentId","uploadedBy","uploadDate") VALUES($1,$2,$3,$4,$5,$6,$7,$8,$9,$10)', [title, subject, topic||'', type||'LINK', filePath, content||'', description||'', target, req.session.user.userId, Date.now()]);
  await notifyStudents(target, '📚 New Study Material', `${title} — ${subject}`, 'NOTES');
  res.json({ ok: true });
});
app.delete('/api/notes/:id', auth, teacherOnly, async (req, res) => {
  const note = await get('SELECT * FROM notes WHERE "noteId"=$1', [parseInt(req.params.id)]);
  if (note?.filePath) try { fs.unlinkSync(path.join(__dirname, note.filePath)); } catch(e){}
  await run('DELETE FROM notes WHERE "noteId"=$1', [parseInt(req.params.id)]);
  res.json({ ok: true });
});

// DOUBTS
app.get('/api/doubts', auth, async (req, res) => {
  const u = req.session.user;
  res.json(u.role === 'TEACHER' ? await all('SELECT * FROM doubts WHERE "targetTeacherId"=$1 OR "targetTeacherId"=\'ALL\' OR "targetTeacherId" IS NULL ORDER BY "createdAt" DESC', [u.userId])
    : await all('SELECT * FROM doubts WHERE "studentId"=$1 ORDER BY "createdAt" DESC', [u.userId]));
});
app.post('/api/doubts', auth, async (req, res) => {
  const { subject, question, targetTeacherId } = req.body;
  const u = req.session.user;
  const target = targetTeacherId || 'ALL';
  await run('INSERT INTO doubts("studentId","studentName",subject,question,"targetTeacherId","createdAt") VALUES($1,$2,$3,$4,$5,$6)', [u.userId, u.name, subject, question, target, Date.now()]);
  if (target === 'ALL') {
    const teachers = await all("SELECT \"userId\" FROM users WHERE role='TEACHER'");
    for (const t of teachers) await notify(t.userId, '❓ New Doubt', `${u.name}: ${subject} — ${question.slice(0,50)}...`, 'DOUBT');
  } else {
    await notify(target, '❓ Direct Doubt', `${u.name} asked you: ${subject} — ${question.slice(0,50)}...`, 'DOUBT');
  }
  res.json({ ok: true });
});
app.put('/api/doubts/:id/reply', auth, teacherOnly, async (req, res) => {
  const doubt = await get('SELECT * FROM doubts WHERE "doubtId"=$1', [parseInt(req.params.id)]);
  await run('UPDATE doubts SET reply=$1,"repliedBy"=$2,"replyDate"=$3,status=$4 WHERE "doubtId"=$5', [req.body.reply, req.session.user.userId, Date.now(), 'RESOLVED', parseInt(req.params.id)]);
  if (doubt) await notify(doubt.studentId, '💬 Doubt Answered', `Your ${doubt.subject} doubt has been answered!`, 'DOUBT_REPLY');
  res.json({ ok: true });
});

// TESTS
app.get('/api/tests', auth, async (req, res) => {
  const u = req.session.user;
  res.json(u.role === 'TEACHER' ? await all('SELECT * FROM tests ORDER BY date DESC')
    : await all("SELECT * FROM tests WHERE \"targetStudentId\"='ALL' OR LOWER(\"targetStudentId\")=$1 ORDER BY date DESC", [u.userId.toLowerCase()]));
});
app.post('/api/tests', auth, teacherOnly, async (req, res) => {
  const { subject, title, date, time, syllabus, totalMarks, targetStudentId } = req.body;
  const target = (targetStudentId && targetStudentId.toUpperCase() !== 'ALL') ? targetStudentId.toLowerCase() : 'ALL';
  await run('INSERT INTO tests(subject,title,date,time,syllabus,"totalMarks","targetStudentId","createdBy","createdAt") VALUES($1,$2,$3,$4,$5,$6,$7,$8,$9)', [subject, title, date, time||'10:00 AM', syllabus||'', totalMarks||100, target, req.session.user.userId, Date.now()]);
  await notifyStudents(target, '📝 New Test Scheduled', `${title} — ${subject} on ${new Date(date).toLocaleDateString('en-IN')}`, 'TEST');
  res.json({ ok: true });
});
app.delete('/api/tests/:id', auth, teacherOnly, async (req, res) => {
  await run('DELETE FROM tests WHERE "testId"=$1', [parseInt(req.params.id)]);
  res.json({ ok: true });
});

// RESULTS
app.get('/api/results', auth, async (req, res) => {
  const u = req.session.user;
  res.json(u.role === 'TEACHER'
    ? await all('SELECT r.*,u.name as "studentName" FROM results r LEFT JOIN users u ON r."studentId"=u."userId" ORDER BY r.date DESC')
    : await all('SELECT r.* FROM results r WHERE LOWER(r."studentId")=$1 ORDER BY r.date DESC', [u.userId.toLowerCase()]));
});
app.post('/api/results', auth, teacherOnly, async (req, res) => {
  const { testName, studentId, marksObtained, totalMarks, remarks } = req.body;
  if (!studentId || !marksObtained) return res.status(400).json({ error: 'Student and marks required' });
  const sid = studentId.toLowerCase();
  try {
    await run('INSERT INTO results("testName","studentId","marksObtained","totalMarks",remarks,date) VALUES($1,$2,$3,$4,$5,$6)', [testName||'Test', sid, parseInt(marksObtained), parseInt(totalMarks)||100, remarks||'', Date.now()]);
    const pct = Math.round(parseInt(marksObtained)/(parseInt(totalMarks)||100)*100);
    await notify(sid, '📊 Marks Updated', `${testName||'Test'}: ${marksObtained}/${totalMarks||100} (${pct}%)`, 'MARKS');
    res.json({ ok: true });
  } catch(e) { res.status(500).json({ error: e.message }); }
});

// TEACHER UTILS
app.post('/api/teacher/qr', auth, teacherOnly, qrUpload.single('qr'), async (req, res) => {
  if (!req.file) return res.status(400).json({ error: 'No file' });
  const qrPath = '/uploads/qr/' + req.file.filename;
  await run('UPDATE users SET "qrCodePath"=$1 WHERE "userId"=$2', [qrPath, req.session.user.userId]);
  res.json({ qrPath });
});
app.get('/api/teacher/qr', auth, async (req, res) => {
  const t = await get("SELECT \"qrCodePath\" FROM users WHERE role='TEACHER' LIMIT 1");
  res.json({ qrPath: t?.qrCodePath || '' });
});
app.post('/api/teacher/name', auth, teacherOnly, async (req, res) => {
  const { name } = req.body;
  if (!name) return res.status(400).json({ error: 'Name required' });
  await run('UPDATE users SET name=$1 WHERE "userId"=$2', [name, req.session.user.userId]);
  req.session.user.name = name;
  res.json({ ok: true });
});

// STATS
app.get('/api/teachers', auth, async (req, res) => {
  res.json(await all("SELECT \"userId\",name FROM users WHERE role='TEACHER' ORDER BY name"));
});
app.get('/api/stats', auth, async (req, res) => {
  const students = (await get("SELECT COUNT(*) as c FROM users WHERE role='STUDENT'"))?.c || 0;
  const pendingFees = (await get("SELECT COUNT(*) as c FROM fees WHERE status='UNPAID'"))?.c || 0;
  const openDoubts = (await get("SELECT COUNT(*) as c FROM doubts WHERE status='OPEN'"))?.c || 0;
  const upcomingTests = (await get('SELECT COUNT(*) as c FROM tests WHERE date>$1', [Date.now()]))?.c || 0;
  res.json({ students: parseInt(students), pendingFees: parseInt(pendingFees), openDoubts: parseInt(openDoubts), upcomingTests: parseInt(upcomingTests) });
});

// ADMIN - Teacher Management
app.get('/api/admin/teachers', auth, adminOnly, async (req, res) => {
  res.json(await all("SELECT \"userId\",name,phone,\"createdAt\" FROM users WHERE role='TEACHER' ORDER BY name"));
});
app.post('/api/admin/teachers', auth, adminOnly, async (req, res) => {
  const { userId, name, password, phone } = req.body;
  const uid = userId.toLowerCase().trim();
  console.log(`[Admin] Adding teacher: ${uid}`);
  if (!uid || !name || !password) return res.status(400).json({ error: 'User ID, Name and Password are required' });
  const exists = await get('SELECT "userId" FROM users WHERE LOWER("userId")=$1', [uid]);
  if (exists) return res.status(400).json({ error: 'User ID already exists' });
  await run('INSERT INTO users("userId",name,role,password,phone,"createdAt") VALUES($1,$2,$3,$4,$5,$6)', [uid, name, 'TEACHER', password, phone||'', Date.now()]);
  res.json({ ok: true, userId: uid });
});
app.delete('/api/admin/teachers/:id', auth, adminOnly, async (req, res) => {
  const uid = req.params.id.toLowerCase();
  console.log(`[Admin] Deleting teacher: ${uid}`);
  await run("DELETE FROM users WHERE LOWER(\"userId\")=$1 AND role='TEACHER'", [uid]);
  res.json({ ok: true });
});
app.get('/api/admin/stats', auth, adminOnly, async (req, res) => {
  const teachers = (await get("SELECT COUNT(*) as c FROM users WHERE role='TEACHER'"))?.c || 0;
  const students = (await get("SELECT COUNT(*) as c FROM users WHERE role='STUDENT'"))?.c || 0;
  res.json({ teachers: parseInt(teachers), students: parseInt(students) });
});

async function start() {
  await initDB();
  app.listen(PORT, () => console.log(`\n  🟢 ShreeJi Classes running on port ${PORT}\n`));
}
start();
