// Prevents additional console window on Windows in release, DO NOT REMOVE!!
#![cfg_attr(not(debug_assertions), windows_subsystem = "windows")]

use tauri::api::dialog;
use tauri::api::path;
use tauri::api::process;
use tauri::api::path::PathDir;
use tauri::api::path::PathItem;

#[tauri::command]
fn tauri_api(
    operation: String,
    scaffold_path: Option<String>,
    flags: Option<Vec<String>>,
) -> Result<Option<String>, String> {
    match operation.as_str() {
        "openScaffoldDirectory" => {
            let result = dialog::open_directory_dialog(Default::default())
                .map_err(|e| format!("Error opening directory dialog: {}", e))?;
            Ok(result.map(|p| p.display().to_string()))
        }
        "getRootPath" => Ok(Some(path::app_dir().to_string_lossy().to_string())),
        "path.join" => {
            let joined_path = path::join(&[&scaffold_path.unwrap()]);
            Ok(Some(joined_path.to_string_lossy().to_string()))
        }
        "path.relative" => {
            let relative_path = path::relative(&[&scaffold_path.unwrap(), &flags.unwrap()[0]]);
            Ok(Some(relative_path.to_string_lossy().to_string()))
        }
        "path.dirname" => {
            let dirname = path::dirname(&[&scaffold_path.unwrap()]);
            Ok(Some(dirname.to_string_lossy().to_string()))
        }
        "path.sep" => Ok(Some(std::path::MAIN_SEPARATOR.to_string())),
        "fs.existsSync" => Ok(Some(
            path::exists(&[PathItem::File(scaffold_path.unwrap().into())])
                .to_string()
                .to_lowercase(),
        )),
        "fs.mkdirSync" => {
            path::create_dir(&[PathDir::Dir(scaffold_path.unwrap().into())])
                .map_err(|e| format!("Error creating directory: {}", e))?;
            Ok(None)
        }
        "fs.getFiles" => {
            let files = path::read_dir(&[PathItem::Dir(scaffold_path.unwrap().into())])
                .map_err(|e| format!("Error reading directory: {}", e))?;
            Ok(Some(files.join(",")))
        }
        "child_process.spawn" => {
            let wrapper_path = path::join(&[&scaffold_path.unwrap(), "gradlew"]);
            let result = process::Command::new(wrapper_path)
                .args(flags.unwrap())
                .spawn()
                .map_err(|e| format!("Error spawning process: {}", e))?;
            Ok(Some(result.id().to_string()))
        }
        "child_process.kill" => {
            let pid = scaffold_path.unwrap().parse().unwrap();
            process::kill(pid).map_err(|e| format!("Error killing process: {}", e))?;
            Ok(None)
        }
        _ => Err(format!("Invalid ipc API operation: {}", operation)),
    }
}

fn main() {
    tauri::Builder::default()
        .invoke_handler(tauri::generate_handler![tauri_api])
        .run(tauri::generate_context!(), tauri::generate_handler![
            tauri::command::preload(include_str!("../tauri-bridge.js")),
        ])
        .expect("error while running tauri application");
}
