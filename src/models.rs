use chrono::{NaiveDate, DateTime, Utc};
use serde::{Serialize, Deserialize};
use sqlx::FromRow;

// ==================== GILET ====================

#[derive(Debug, Clone, Serialize, Deserialize, FromRow)]
pub struct Gilet {
    pub id: i64,
    pub num_serie: String,
    pub modele: String,
    pub taille: String,
    pub date_achat: NaiveDate,
    pub etat: String,
    pub dernier_test: Option<NaiveDate>,
}

#[derive(Debug, Clone, Deserialize)]
pub struct NewGilet {
    pub num_serie: String,
    pub modele: String,
    pub taille: String,
    pub date_achat: NaiveDate,
    pub etat: String,
    pub dernier_test: Option<NaiveDate>,
}

#[derive(Debug, Clone, Deserialize)]
pub struct UpdateGilet {
    pub num_serie: Option<String>,
    pub modele: Option<String>,
    pub taille: Option<String>,
    pub date_achat: Option<NaiveDate>,
    pub etat: Option<String>,
    pub dernier_test: Option<NaiveDate>,
}

// ==================== TEST FLOTTABILITE ====================

#[derive(Debug, Clone, Serialize, Deserialize, FromRow)]
pub struct TestFlottabilite {
    pub id: i64,
    pub gilet_id: i64,
    pub date_test: NaiveDate,
    pub resultat: String,
    pub commentaire: Option<String>,
    pub responsable: String,
}

#[derive(Debug, Clone, Deserialize)]
pub struct NewTestFlottabilite {
    pub gilet_id: i64,
    pub date_test: NaiveDate,
    pub resultat: String,
    pub commentaire: Option<String>,
    pub responsable: String,
}

#[derive(Debug, Clone, Deserialize)]
pub struct UpdateTestFlottabilite {
    pub gilet_id: Option<i64>,
    pub date_test: Option<NaiveDate>,
    pub resultat: Option<String>,
    pub commentaire: Option<String>,
    pub responsable: Option<String>,
}

// ==================== DTOs ====================

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct GiletWithTests {
    pub gilet: Gilet,
    pub tests: Vec<TestFlottabilite>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ApiResponse<T> {
    pub success: bool,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub data: Option<T>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub message: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub error: Option<String>,
}

impl<T> ApiResponse<T> {
    pub fn success(data: T) -> Self {
        ApiResponse {
            success: true,
            data: Some(data),
            message: None,
            error: None,
        }
    }

    pub fn error(error: String) -> Self {
        ApiResponse {
            success: false,
            data: None,
            message: None,
            error: Some(error),
        }
    }

    pub fn message(message: String) -> Self {
        ApiResponse {
            success: true,
            data: None,
            message: Some(message),
            error: None,
        }
    }
}
