use actix_web::{web, HttpResponse, Responder};
use rusqlite::Connection;

use crate::models::{ApiResponse, Gilet, NewGilet, UpdateGilet};
use crate::db::*;

pub fn configure(cfg: &mut web::ServiceConfig) {
    cfg.service(
        web::scope("/gilets")
            .route("", web::get().to(get_all))
            .route("", web::post().to(create))
            .route("/{id}", web::get().to(get_by_id))
            .route("/{id}", web::put().to(update))
            .route("/{id}", web::delete().to(delete))
            .route("/search", web::get().to(search)),
    );
}

pub async fn get_all(db_path: web::Data<String>) -> impl Responder {
    let conn = match get_connection(&db_path) {
        Ok(c) => c,
        Err(e) => return HttpResponse::InternalServerError().json(ApiResponse::<()>::error(format!("DB error: {}", e))),
    };

    match get_all_gilets(&conn) {
        Ok(gilets) => HttpResponse::Ok().json(ApiResponse::success(gilets)),
        Err(e) => HttpResponse::InternalServerError().json(ApiResponse::<()>::error(format!("Error: {}", e))),
    }
}

pub async fn get_by_id(id: web::Path<i64>, db_path: web::Data<String>) -> impl Responder {
    let conn = match get_connection(&db_path) {
        Ok(c) => c,
        Err(e) => return HttpResponse::InternalServerError().json(ApiResponse::<()>::error(format!("DB error: {}", e))),
    };

    match get_gilet_by_id(&conn, id.into_inner()) {
        Ok(Some(gilet)) => HttpResponse::Ok().json(ApiResponse::success(gilet)),
        Ok(None) => HttpResponse::NotFound().json(ApiResponse::<()>::error("Gilet non trouvé".to_string())),
        Err(e) => HttpResponse::InternalServerError().json(ApiResponse::<()>::error(format!("Error: {}", e))),
    }
}

pub async fn create(gilet: web::Json<NewGilet>, db_path: web::Data<String>) -> impl Responder {
    let conn = match get_connection(&db_path) {
        Ok(c) => c,
        Err(e) => return HttpResponse::InternalServerError().json(ApiResponse::<()>::error(format!("DB error: {}", e))),
    };

    match create_gilet(&conn, &gilet) {
        Ok(new_gilet) => HttpResponse::Created().json(ApiResponse::success(new_gilet)),
        Err(e) => HttpResponse::BadRequest().json(ApiResponse::<()>::error(format!("Error: {}", e))),
    }
}

pub async fn update(
    id: web::Path<i64>,
    gilet: web::Json<UpdateGilet>,
    db_path: web::Data<String>,
) -> impl Responder {
    let conn = match get_connection(&db_path) {
        Ok(c) => c,
        Err(e) => return HttpResponse::InternalServerError().json(ApiResponse::<()>::error(format!("DB error: {}", e))),
    };

    match update_gilet(&conn, id.into_inner(), &gilet) {
        Ok(Some(updated)) => HttpResponse::Ok().json(ApiResponse::success(updated)),
        Ok(None) => HttpResponse::NotFound().json(ApiResponse::<()>::error("Gilet non trouvé".to_string())),
        Err(e) => HttpResponse::InternalServerError().json(ApiResponse::<()>::error(format!("Error: {}", e))),
    }
}

pub async fn delete(id: web::Path<i64>, db_path: web::Data<String>) -> impl Responder {
    let conn = match get_connection(&db_path) {
        Ok(c) => c,
        Err(e) => return HttpResponse::InternalServerError().json(ApiResponse::<()>::error(format!("DB error: {}", e))),
    };

    match delete_gilet(&conn, id.into_inner()) {
        Ok(true) => HttpResponse::Ok().json(ApiResponse::<()>::message("Gilet supprimé avec succès".to_string())),
        Ok(false) => HttpResponse::NotFound().json(ApiResponse::<()>::error("Gilet non trouvé".to_string())),
        Err(e) => HttpResponse::InternalServerError().json(ApiResponse::<()>::error(format!("Error: {}", e))),
    }
}

pub async fn search(query: web::Query<(String, String)>, db_path: web::Data<String>) -> impl Responder {
    let (field, value) = query.into_inner();
    let conn = match get_connection(&db_path) {
        Ok(c) => c,
        Err(e) => return HttpResponse::InternalServerError().json(ApiResponse::<()>::error(format!("DB error: {}", e))),
    };

    match search_gilets(&conn, &field, &value) {
        Ok(gilets) => HttpResponse::Ok().json(ApiResponse::success(gilets)),
        Err(e) => HttpResponse::BadRequest().json(ApiResponse::<()>::error(format!("Error: {}", e))),
    }
}
