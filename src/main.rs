use actix_cors::Cors;
use actix_files as fs;
use actix_web::{web, App, HttpServer};
use dotenvy::dotenv;
use std::env;

mod db;
mod models;
mod routes;
mod utils;

use db::init_db;
use routes::{gilets, tests};

#[actix_web::main]
async fn main() -> std::io::Result<()> {
    dotenv().ok();

    // Initialize database
    let db_path = env::var("DATABASE_URL").unwrap_or_else(|_| "gik.db".to_string());
    init_db(&db_path).expect("Failed to initialize database");

    println!("🚀 Démarrage du serveur GIK Backend...");
    println!("📍 Accès : http://localhost:8080");
    println!("📊 API : http://localhost:8080/api");

    HttpServer::new(move || {
        let cors = Cors::default()
            .allow_any_origin()
            .allow_any_method()
            .allow_any_header()
            .max_age(3600);

        App::new()
            .wrap(cors)
            .app_data(web::Data::new(db_path.clone()))
            .service(
                web::scope("/api")
                    .configure(gilets::configure)
                    .configure(tests::configure),
            )
            // Serve static files
            .service(fs::Files::new("/", "static/").index_file("index.html"))
            .service(
                web::resource("/health").to(|| async { "✅ Server is running" }),
            )
    })
    .bind("0.0.0.0:8080")?
    .run()
    .await
}
