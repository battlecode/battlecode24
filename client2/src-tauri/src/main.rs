// Prevents additional console window on Windows in release, DO NOT REMOVE!!
#![cfg_attr(not(debug_assertions), windows_subsystem = "windows")]

use std::path;
use std::path::Path;
use std::process::Command;
use tauri::api::{dialog, path as tpath};

#[tauri::command]
fn tauri_api(
    operation: String,
    scaffold_path: Option<String>,
    flags: Option<Vec<String>>,
) -> Result<Option<String>, String> {
    match operation.as_str() {
        "openScaffoldDirectory" => {
            let directory_path = dialog::blocking::FileDialogBuilder::new()
                .pick_folder()
                .expect("Failed to show directory picker");
            Ok(Some(directory_path.to_string_lossy().to_string()))
        }

        "getRootPath" => Ok(Some(
            tpath::app_data_dir(&tauri::generate_context!().config())
                .map(|p| p.to_string_lossy().to_string())
                .unwrap_or_else(|| "".to_string()),
        )),
        "path.join" => {
            let joined_path = path::PathBuf::from(&scaffold_path.unwrap());
            Ok(Some(joined_path.to_string_lossy().to_string()))
        }
        "path.relative" => {
            let scaffold_path = Path::new(&scaffold_path.unwrap());
            let flag_path = flags
                .as_ref()
                .and_then(|f| f.first())
                .map(Path::new)
                .unwrap_or_else(|| Path::new(""));

            let mut relative_path = Path::new("");

            for (s, f) in scaffold_path.components().zip(flag_path.components()) {
                if s != f {
                    relative_path = scaffold_path
                        .strip_prefix(flag_path)
                        .unwrap_or(Path::new(""));
                    break;
                }
            }

            Ok(Some(relative_path.to_string_lossy().to_string()))
        }
        "path.dirname" => {
            let dirname = scaffold_path
                .as_deref()
                .and_then(|p| std::path::Path::new(p).parent())
                .map(|p| p.to_string_lossy().to_string())
                .unwrap_or_else(|| "".to_string());
            Ok(Some(dirname))
        }               
        "path.sep" => Ok(Some(path::MAIN_SEPARATOR.to_string())),
        "fs.existsSync" => Ok(Some(
            path::Path::new(&scaffold_path.unwrap())
                .exists()
                .to_string()
                .to_lowercase(),
        )),
        "fs.mkdirSync" => {
            std::fs::create_dir(&scaffold_path.unwrap())
                .map_err(|e| format!("Error creating directory: {}", e))?;
            Ok(None)
        }        
        "fs.getFiles" => {
            let files = path::Path::new(&scaffold_path.unwrap())
                .read_dir()
                .map_err(|e| format!("Error reading directory: {}", e))?
                .map(|entry| entry.unwrap().file_name().to_string_lossy().to_string())
                .collect::<Vec<_>>()
                .join(",");
            Ok(Some(files))
        }
        "child_process.spawn" => {
            let wrapper_path = path::PathBuf::from(&scaffold_path.unwrap()).join("gradlew");
            let result = Command::new(wrapper_path)
                .args(flags.unwrap())
                .spawn()
                .map_err(|e| format!("Error spawning process: {}", e))?;
            Ok(Some(result.id().to_string()))
        }
        "child_process.kill" => {
            let pid = scaffold_path.unwrap().parse().unwrap();
            Command::new("kill").arg(pid.to_string()).status()?;
            Ok(None)
        }
        _ => Err(format!("Invalid ipc API operation: {}", operation)),
    }
}

fn main() {
    tauri::Builder::default()
        .invoke_handler(tauri::generate_handler![tauri_api])
        .invoke_handler(tauri::generate_handler![tauri::command::preload(
            include_str!("../tauri-bridge.js")
        )])
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
