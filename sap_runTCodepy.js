const { PythonShell } = require('python-shell');


let scriptKey = process.argv[2]; // e.g., "ZVM01" or "zecomrecon"
let extraArg = process.argv[3]; 

// Construct the full Python file name
let scriptFile = `sap_runner_1_${scriptKey}.py`;

// if(extraArg) {
let options = {
  pythonPath: "d:\\Users\\riat\\AppData\\Local\\Microsoft\\WindowsApps\\python.exe",
  args: [extraArg]
};
// } else {
//   let options = {
//   pythonPath: "d:\\Users\\riat\\AppData\\Local\\Microsoft\\WindowsApps\\python.exe"
// };
// }

let pyshell = new PythonShell(scriptFile, options);

//let pyshell = new PythonShell('sap_runner_zecomrecon.py', { pythonPath: "d:\\Users\\riat\\AppData\\Local\\Microsoft\\WindowsApps\\python.exe" });

pyshell.on('message', message => {
  console.log('🐍 Python says:', message);
});

pyshell.on('stderr', err => {
  console.error('Python error:', err.toString());
});

pyshell.end(err => {
  if (err) console.error('Process ended with error:', err);
  else console.log('✅ Python script finished.');
});