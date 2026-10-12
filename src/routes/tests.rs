use actix_web::{web, HttpResponse, Responder};

use crate::models::{ApiResponse, NewTestFlottabilite, TestFlottabilite, UpdateTestFlottabilite};
use crate::db::*;

pub fn configure(cfg: &mut web::ServiceConfig) {
    cfg.service(
        web::scope("/tests")
            .route("", web::get().to(get_all))
            .route("", web::post().to(create))
            .route("/{id}", web::get().to(get_by_id))
            .route("/{id}", web::put().to(update))
            .route("/{id}", web::delete().to(delete))
            .route("/by-gilet/{gilet_id}", web::get().to(get_by_gilet)),
    );
}

pub async fn get_all(db_path: web::Data<String>) -> impl Responder {
    let conn = match get_connection(&db_path) {
        Ok(c) => c,
        Err(e) => return HttpResponse::InternalServerError().json(ApiResponse::<()>::error(format!("DB error: {}", e))),
    };

    match get_all_tests(&conn) {
        Ok(tests) => HttpResponse::Ok().json(ApiResponse::success(tests)),
        Err(e) => HttpResponse::InternalServerError().json(ApiResponse::<()>::error(format!("Error: {}", e))),
    }
}

pub async fn get_by_id(id: web::Path<i64>, db_path: web::Data<String>) -> impl Responder {
    let conn = match get_connection(&db_path) {
        Ok(c) => c,
        Err(e) => return HttpResponse::InternalServerError().json(ApiResponse::<()>::error(format!("DB error: {}", e))),
    };

    match get_test_by_id(&conn, id.into_inner()) {
        Ok(Some(test)) => HttpResponse::Ok().json(ApiResponse::success(test)),
        Ok(None) => HttpResponse::NotFound().json(ApiResponse::<()>::error("Test non trouvé".to_string())),
        Err(e) => HttpResponse::InternalServerError().json(ApiResponse::<()>::error(format!("Error: {}", e))),
    }
}

pub async fn create(test: web::Json<NewTestFlottabilite>, db_path: web::Data<String>) -> impl Responder {
    let conn = match get_connection(&db_path) {
        Ok(c) => c,
        Err(e) => return HttpResponse::InternalServerError().json(ApiResponse::<()>::error(format!("DB error: {}", e))),
    };

    match create_test(&conn, &test) {
        Ok(new_test) => HttpResponse::Created().json(ApiResponse::success(new_test)),
        Err(e) => HttpResponse::BadRequest().json(ApiResponse::<()>::error(format!("Error: {}", e))),
    }
}

pub async fn update(
    id: web::Path<i64>,
    test: web::Json<UpdateTestFlottabilite>,
    db_path: web::Data<String>,
) -> impl Responder {
    let conn = match get_connection(&db_path) {
        Ok(c) => c,
        Err(e) => return HttpResponse::InternalServerError().json(ApiResponse::<()>::error(format!("DB error: {}", e))),
    };

    match update_test(&conn, id.into_inner(), &test) {
        Ok(Some(updated)) => HttpResponse::Ok().json(ApiResponse::success(updated)),
        Ok(None) => HttpResponse::NotFound().json(ApiResponse::<()>::error("Test non trouvé".to_string())),
        Err(e) => HttpResponse::InternalServerError().json(ApiResponse::<()>::error(format!("Error: {}", e))),
    }
}

pub async fn delete(id: web::Path<i64>, db_path: web::Data<String>) -> impl Responder {
    let conn = match get_connection(&db_path) {
        Ok(c) => c,
        Err(e) => return HttpResponse::InternalServerError().json(ApiResponse::<()>::error(format!("DB error: {}", e))),
    };

    match delete_test(&conn, id.into_inner()) {
        Ok(true) => HttpResponse::Ok().json(ApiResponse::<()>::message("Test supprimé avec succès".to_string())),
        Ok(false) => HttpResponse::NotFound().json(ApiResponse::<()>::error("Test non trouvé".to_string())),
        Err(e) => HttpResponse::InternalServerError().json(ApiResponse::<()>::error(format!("Error: {}", e))),
    }
}

pub async fn get_by_gilet(gilet_id: web::Path<i64>, db_path: web::Data<String>) -> impl Responder {
    let conn = match get_connection(&db_path) {
        Ok(c) => c,
        Err(e) => return HttpResponse::InternalServerError().json(ApiResponse::<()>::error(format!("DB error: {}", e))),
    };

    match get_tests_by_gilet(&conn, gilet_id.into_inner()) {
        Ok(tests) => HttpResponse::Ok().json(ApiResponse::success(tests)),
        Err(e) => HttpResponse::InternalServerError().json(ApiResponse::<()>::error(format!("Error: {}", e))),
    }
}
