const { Pool } = require('pg');

const pool = new Pool({
  connectionString: process.env.DATABASE_URL || 'postgresql://localhost:5432/shreeji',
  ssl: process.env.DATABASE_URL ? { rejectUnauthorized: false } : false
});

async function initDB() {
  const client = await pool.connect();
  try {
    await client.query(`CREATE TABLE IF NOT EXISTS users (
      "userId" TEXT PRIMARY KEY, name TEXT NOT NULL, role TEXT NOT NULL, password TEXT NOT NULL,
      phone TEXT DEFAULT '', "parentPhone" TEXT DEFAULT '', "studentClass" TEXT DEFAULT '',
      "monthlyFee" REAL DEFAULT 0, "qrCodePath" TEXT DEFAULT '', "createdAt" BIGINT DEFAULT 0
    )`);
    await client.query(`CREATE TABLE IF NOT EXISTS fees (
      "feeId" SERIAL PRIMARY KEY, "studentId" TEXT NOT NULL, month INTEGER NOT NULL, year INTEGER NOT NULL,
      amount REAL NOT NULL, status TEXT DEFAULT 'UNPAID', "paidAmount" REAL DEFAULT 0,
      "paidDate" BIGINT, "dueDate" BIGINT, remarks TEXT DEFAULT ''
    )`);
    await client.query(`CREATE TABLE IF NOT EXISTS notes (
      "noteId" SERIAL PRIMARY KEY, title TEXT NOT NULL, subject TEXT NOT NULL, topic TEXT DEFAULT '',
      type TEXT DEFAULT 'LINK', "filePath" TEXT DEFAULT '', content TEXT DEFAULT '',
      description TEXT DEFAULT '', "targetStudentId" TEXT DEFAULT 'ALL',
      "uploadedBy" TEXT NOT NULL, "uploadDate" BIGINT DEFAULT 0
    )`);
    try { await client.query('ALTER TABLE users ADD COLUMN "fcmToken" TEXT DEFAULT \'\''); } catch(e){}
    await client.query(`CREATE TABLE IF NOT EXISTS doubts (
      "doubtId" SERIAL PRIMARY KEY, "studentId" TEXT NOT NULL, "studentName" TEXT DEFAULT '',
      subject TEXT NOT NULL, question TEXT NOT NULL, reply TEXT, "repliedBy" TEXT,
      "replyDate" BIGINT, status TEXT DEFAULT 'OPEN', "createdAt" BIGINT DEFAULT 0
    )`);
    try { await client.query('ALTER TABLE doubts ADD COLUMN "targetTeacherId" TEXT DEFAULT \'ALL\''); } catch(e){}
    await client.query(`CREATE TABLE IF NOT EXISTS tests (
      "testId" SERIAL PRIMARY KEY, subject TEXT NOT NULL, title TEXT NOT NULL, date BIGINT NOT NULL,
      time TEXT DEFAULT '10:00 AM', syllabus TEXT DEFAULT '', "totalMarks" INTEGER DEFAULT 100,
      "targetStudentId" TEXT DEFAULT 'ALL', "createdBy" TEXT NOT NULL, "createdAt" BIGINT DEFAULT 0
    )`);
    await client.query(`CREATE TABLE IF NOT EXISTS results (
      "resultId" SERIAL PRIMARY KEY, "testId" INTEGER, "testName" TEXT DEFAULT '',
      "studentId" TEXT NOT NULL, "marksObtained" INTEGER NOT NULL,
      "totalMarks" INTEGER DEFAULT 100, remarks TEXT DEFAULT '', date BIGINT DEFAULT 0
    )`);
    await client.query(`CREATE TABLE IF NOT EXISTS notifications (
      "notifId" SERIAL PRIMARY KEY, "userId" TEXT NOT NULL, title TEXT NOT NULL,
      message TEXT DEFAULT '', type TEXT DEFAULT 'INFO', read BOOLEAN DEFAULT false,
      "createdAt" BIGINT DEFAULT 0
    )`);

    // Seed admin
    const { rows } = await client.query('SELECT COUNT(*) as c FROM users');
    const cnt = parseInt(rows[0].c);
    console.log('User count:', cnt);
    if (cnt === 0) {
      await client.query(
        `INSERT INTO users("userId",name,role,password,"createdAt") VALUES($1,$2,$3,$4,$5)`,
        ['admin', 'Super Admin', 'ADMIN', 'admin123', Date.now()]
      );
    }
  } finally { client.release(); }
}

async function all(q, p) {
  const { rows } = await pool.query(q, p);
  return rows;
}
async function get(q, p) {
  const { rows } = await pool.query(q, p);
  return rows[0] || null;
}
async function run(q, p) {
  await pool.query(q, p);
}

module.exports = { initDB, all, get, run, pool };
