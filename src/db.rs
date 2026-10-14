use sqlx::sqlite::SqlitePool;
use sqlx::{FromRow, Result, query, query_as};

use crate::models::{Gilet, NewGilet, UpdateGilet, TestFlottabilite, NewTestFlottabilite, UpdateTestFlottabilite};

pub async fn init_db(db_url: &str) -> Result<SqlitePool> {
    let pool = SqlitePool::connect(db_url).await?;
    
    // Create tables
    query(
        "CREATE TABLE IF NOT EXISTS gilets (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            num_serie TEXT NOT NULL UNIQUE,
            modele TEXT NOT NULL,
            taille TEXT NOT NULL,
            date_achat DATE NOT NULL,
            etat TEXT NOT NULL DEFAULT 'Bon',
            dernier_test DATE
        )",
    ).execute(&pool).await?;

    query(
        "CREATE TABLE IF NOT EXISTS tests_flottabilite (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            gilet_id INTEGER NOT NULL,
            date_test DATE NOT NULL,
            resultat TEXT NOT NULL CHECK(resultat IN ('OK', 'KO')),
            commentaire TEXT,
            responsable TEXT NOT NULL,
            FOREIGN KEY (gilet_id) REFERENCES gilets(id) ON DELETE CASCADE
        )",
    ).execute(&pool).await?;

    // Create indexes
    query("CREATE INDEX IF NOT EXISTS idx_gilets_num_serie ON gilets(num_serie)")
        .execute(&pool).await?;
    query("CREATE INDEX IF NOT EXISTS idx_gilets_modele ON gilets(modele)")
        .execute(&pool).await?;
    query("CREATE INDEX IF NOT EXISTS idx_tests_gilet_id ON tests_flottabilite(gilet_id)")
        .execute(&pool).await?;
    query("CREATE INDEX IF NOT EXISTS idx_tests_date ON tests_flottabilite(date_test)")
        .execute(&pool).await?;

    Ok(pool)
}

// ==================== GILET CRUD ====================

pub async fn get_all_gilets(pool: &SqlitePool) -> Result<Vec<Gilet>> {
    let gilets = query_as::<_, Gilet>(
        "SELECT id, num_serie, modele, taille, date_achat, etat, dernier_test FROM gilets"
    )
    .fetch_all(pool)
    .await?;
    
    Ok(gilets)
}

pub async fn get_gilet_by_id(pool: &SqlitePool, id: i64) -> Result<Option<Gilet>> {
    let gilet = query_as::<_, Gilet>(
        "SELECT id, num_serie, modele, taille, date_achat, etat, dernier_test FROM gilets WHERE id = ?"
    )
    .bind(id)
    .fetch_optional(pool)
    .await?;
    
    Ok(gilet)
}

pub async fn create_gilet(pool: &SqlitePool, gilet: &NewGilet) -> Result<Gilet> {
    let new_gilet = query_as::<_, Gilet>(
        "INSERT INTO gilets (num_serie, modele, taille, date_achat, etat, dernier_test) 
         VALUES (?, ?, ?, ?, ?, ?)
         RETURNING id, num_serie, modele, taille, date_achat, etat, dernier_test"
    )
    .bind(&gilet.num_serie)
    .bind(&gilet.modele)
    .bind(&gilet.taille)
    .bind(&gilet.date_achat)
    .bind(&gilet.etat)
    .bind(&gilet.dernier_test)
    .fetch_one(pool)
    .await?;
    
    Ok(new_gilet)
}

pub async fn update_gilet(pool: &SqlitePool, id: i64, gilet: &UpdateGilet) -> Result<Option<Gilet>> {
    let existing = get_gilet_by_id(pool, id).await?;
    if existing.is_none() {
        return Ok(None);
    }
    
    let mut updated_gilet = existing.unwrap();
    
    if let Some(ref num_serie) = gilet.num_serie {
        query("UPDATE gilets SET num_serie = ? WHERE id = ?")
            .bind(num_serie)
            .bind(id)
            .execute(pool)
            .await?;
        updated_gilet.num_serie = num_serie.clone();
    }
    if let Some(ref modele) = gilet.modele {
        query("UPDATE gilets SET modele = ? WHERE id = ?")
            .bind(modele)
            .bind(id)
            .execute(pool)
            .await?;
        updated_gilet.modele = modele.clone();
    }
    if let Some(ref taille) = gilet.taille {
        query("UPDATE gilets SET taille = ? WHERE id = ?")
            .bind(taille)
            .bind(id)
            .execute(pool)
            .await?;
        updated_gilet.taille = taille.clone();
    }
    if let Some(ref date_achat) = gilet.date_achat {
        query("UPDATE gilets SET date_achat = ? WHERE id = ?")
            .bind(date_achat)
            .bind(id)
            .execute(pool)
            .await?;
        updated_gilet.date_achat = *date_achat;
    }
    if let Some(ref etat) = gilet.etat {
        query("UPDATE gilets SET etat = ? WHERE id = ?")
            .bind(etat)
            .bind(id)
            .execute(pool)
            .await?;
        updated_gilet.etat = etat.clone();
    }
    if let Some(ref dernier_test) = gilet.dernier_test {
        query("UPDATE gilets SET dernier_test = ? WHERE id = ?")
            .bind(dernier_test)
            .bind(id)
            .execute(pool)
            .await?;
        updated_gilet.dernier_test = Some(*dernier_test);
    }
    
    Ok(Some(updated_gilet))
}

pub async fn delete_gilet(pool: &SqlitePool, id: i64) -> Result<bool> {
    let rows = query("DELETE FROM gilets WHERE id = ?")
        .bind(id)
        .execute(pool)
        .await?;
    Ok(rows.rows_affected() > 0)
}

pub async fn search_gilets(pool: &SqlitePool, field: &str, value: &str) -> Result<Vec<Gilet>> {
    let valid_fields = ["num_serie", "modele", "taille", "etat"];
    if !valid_fields.contains(&field) {
        return Err(sqlx::Error::Protocol("Invalid field".into()));
    }
    
    let query_str = format!("SELECT id, num_serie, modele, taille, date_achat, etat, dernier_test FROM gilets WHERE {} = ?", field);
    
    let gilets = query_as::<_, Gilet>(&query_str)
        .bind(value)
        .fetch_all(pool)
        .await?;
    
    Ok(gilets)
}

// ==================== TEST CRUD ====================

pub async fn get_all_tests(pool: &SqlitePool) -> Result<Vec<TestFlottabilite>> {
    let tests = query_as::<_, TestFlottabilite>(
        "SELECT id, gilet_id, date_test, resultat, commentaire, responsable FROM tests_flottabilite"
    )
    .fetch_all(pool)
    .await?;
    
    Ok(tests)
}

pub async fn get_test_by_id(pool: &SqlitePool, id: i64) -> Result<Option<TestFlottabilite>> {
    let test = query_as::<_, TestFlottabilite>(
        "SELECT id, gilet_id, date_test, resultat, commentaire, responsable FROM tests_flottabilite WHERE id = ?"
    )
    .bind(id)
    .fetch_optional(pool)
    .await?;
    
    Ok(test)
}

pub async fn create_test(pool: &SqlitePool, test: &NewTestFlottabilite) -> Result<TestFlottabilite> {
    let new_test = query_as::<_, TestFlottabilite>(
        "INSERT INTO tests_flottabilite (gilet_id, date_test, resultat, commentaire, responsable) 
         VALUES (?, ?, ?, ?, ?)
         RETURNING id, gilet_id, date_test, resultat, commentaire, responsable"
    )
    .bind(&test.gilet_id)
    .bind(&test.date_test)
    .bind(&test.resultat)
    .bind(&test.commentaire)
    .bind(&test.responsable)
    .fetch_one(pool)
    .await?;
    
    Ok(new_test)
}

pub async fn update_test(pool: &SqlitePool, id: i64, test: &UpdateTestFlottabilite) -> Result<Option<TestFlottabilite>> {
    let existing = get_test_by_id(pool, id).await?;
    if existing.is_none() {
        return Ok(None);
    }
    
    let mut updated_test = existing.unwrap();
    
    if let Some(gilet_id) = test.gilet_id {
        query("UPDATE tests_flottabilite SET gilet_id = ? WHERE id = ?")
            .bind(gilet_id)
            .bind(id)
            .execute(pool)
            .await?;
        updated_test.gilet_id = gilet_id;
    }
    if let Some(date_test) = test.date_test {
        query("UPDATE tests_flottabilite SET date_test = ? WHERE id = ?")
            .bind(date_test)
            .bind(id)
            .execute(pool)
            .await?;
        updated_test.date_test = date_test;
    }
    if let Some(ref resultat) = test.resultat {
        query("UPDATE tests_flottabilite SET resultat = ? WHERE id = ?")
            .bind(resultat)
            .bind(id)
            .execute(pool)
            .await?;
        updated_test.resultat = resultat.clone();
    }
    if let Some(ref commentaire) = test.commentaire {
        query("UPDATE tests_flottabilite SET commentaire = ? WHERE id = ?")
            .bind(commentaire)
            .bind(id)
            .execute(pool)
            .await?;
        updated_test.commentaire = Some(commentaire.clone());
    }
    if let Some(ref responsable) = test.responsable {
        query("UPDATE tests_flottabilite SET responsable = ? WHERE id = ?")
            .bind(responsable)
            .bind(id)
            .execute(pool)
            .await?;
        updated_test.responsable = responsable.clone();
    }
    
    Ok(Some(updated_test))
}

pub async fn delete_test(pool: &SqlitePool, id: i64) -> Result<bool> {
    let rows = query("DELETE FROM tests_flottabilite WHERE id = ?")
        .bind(id)
        .execute(pool)
        .await?;
    Ok(rows.rows_affected() > 0)
}

pub async fn get_tests_by_gilet(pool: &SqlitePool, gilet_id: i64) -> Result<Vec<TestFlottabilite>> {
    let tests = query_as::<_, TestFlottabilite>(
        "SELECT id, gilet_id, date_test, resultat, commentaire, responsable 
         FROM tests_flottabilite WHERE gilet_id = ? ORDER BY date_test DESC"
    )
    .bind(gilet_id)
    .fetch_all(pool)
    .await?;
    
    Ok(tests)
}

pub async fn get_gilet_with_tests(pool: &SqlitePool, gilet_id: i64) -> Result<Option<(Gilet, Vec<TestFlottabilite>)>> {
    let gilet = match get_gilet_by_id(pool, gilet_id).await? {
        Some(g) => g,
        None => return Ok(None),
    };
    
    let tests = get_tests_by_gilet(pool, gilet_id).await?;
    Ok(Some((gilet, tests)))
}
