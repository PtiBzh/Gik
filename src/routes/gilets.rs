use actix_web::{web, HttpResponse, Responder};
use sqlx::sqlite::SqlitePool;
use std::sync::Arc;

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

pub async fn get_all(pool: web::Data<Arc<SqlitePool>>) -> impl Responder {
    match get_all_gilets(&pool).await {
        Ok(gilets) => HttpResponse::Ok().json(ApiResponse::success(gilets)),
        Err(e) => HttpResponse::InternalServerError().json(ApiResponse::<()>::error(format!("Error: {}", e))),
    }
}

pub async fn get_by_id(id: web::Path<i64>, pool: web::Data<Arc<SqlitePool>>) -> impl Responder {
    match get_gilet_by_id(&pool, id.into_inner()).await {
        Ok(Some(gilet)) => HttpResponse::Ok().json(ApiResponse::success(gilet)),
        Ok(None) => HttpResponse::NotFound().json(ApiResponse::<()>::error("Gilet non trouvé".to_string())),
        Err(e) => HttpResponse::InternalServerError().json(ApiResponse::<()>::error(format!("Error: {}", e))),
    }
}

pub async fn create(gilet: web::Json<NewGilet>, pool: web::Data<Arc<SqlitePool>>) -> impl Responder {
    match create_gilet(&pool, &gilet).await {
        Ok(new_gilet) => HttpResponse::Created().json(ApiResponse::success(new_gilet)),
        Err(e) => HttpResponse::BadRequest().json(ApiResponse::<()>::error(format!("Error: {}", e))),
    }
}

pub async fn update(
    id: web::Path<i64>,
    gilet: web::Json<UpdateGilet>,
    pool: web::Data<Arc<SqlitePool>>,
) -> impl Responder {
    match update_gilet(&pool, id.into_inner(), &gilet).await {
        Ok(Some(updated)) => HttpResponse::Ok().json(ApiResponse::success(updated)),
        Ok(None) => HttpResponse::NotFound().json(ApiResponse::<()>::error("Gilet non trouvé".to_string())),
        Err(e) => HttpResponse::InternalServerError().json(ApiResponse::<()>::error(format!("Error: {}", e))),
    }
}

pub async fn delete(id: web::Path<i64>, pool: web::Data<Arc<SqlitePool>>) -> impl Responder {
    match delete_gilet(&pool, id.into_inner()).await {
        Ok(true) => HttpResponse::Ok().json(ApiResponse::<()>::message("Gilet supprimé avec succès".to_string())),
        Ok(false) => HttpResponse::NotFound().json(ApiResponse::<()>::error("Gilet non trouvé".to_string())),
        Err(e) => HttpResponse::InternalServerError().json(ApiResponse::<()>::error(format!("Error: {}", e))),
    }
}

pub async fn search(query: web::Query<(String, String)>, pool: web::Data<Arc<SqlitePool>>) -> impl Responder {
    let (field, value) = query.into_inner();
    match search_gilets(&pool, &field, &value).await {
        Ok(gilets) => HttpResponse::Ok().json(ApiResponse::success(gilets)),
        Err(e) => HttpResponse::BadRequest().json(ApiResponse::<()>::error(format!("Error: {}", e))),
    }
}
