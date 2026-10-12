use rusqlite::{Connection, Result};

use crate::models::{Gilet, NewGilet, UpdateGilet, TestFlottabilite, NewTestFlottabilite, UpdateTestFlottabilite};

pub fn init_db(db_path: &str) -> Result<Connection> {
    let conn = Connection::open(db_path)?;
    
    // Create tables
    conn.execute(
        "CREATE TABLE IF NOT EXISTS gilets (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            num_serie TEXT NOT NULL UNIQUE,
            modele TEXT NOT NULL,
            taille TEXT NOT NULL,
            date_achat DATE NOT NULL,
            etat TEXT NOT NULL DEFAULT 'Bon',
            dernier_test DATE
        )",
        [],
    )?;

    conn.execute(
        "CREATE TABLE IF NOT EXISTS tests_flottabilite (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            gilet_id INTEGER NOT NULL,
            date_test DATE NOT NULL,
            resultat TEXT NOT NULL CHECK(resultat IN ('OK', 'KO')),
            commentaire TEXT,
            responsable TEXT NOT NULL,
            FOREIGN KEY (gilet_id) REFERENCES gilets(id) ON DELETE CASCADE
        )",
        [],
    )?;

    // Create indexes
    conn.execute(
        "CREATE INDEX IF NOT EXISTS idx_gilets_num_serie ON gilets(num_serie)",
        [],
    )?;
    conn.execute(
        "CREATE INDEX IF NOT EXISTS idx_gilets_modele ON gilets(modele)",
        [],
    )?;
    conn.execute(
        "CREATE INDEX IF NOT EXISTS idx_tests_gilet_id ON tests_flottabilite(gilet_id)",
        [],
    )?;
    conn.execute(
        "CREATE INDEX IF NOT EXISTS idx_tests_date ON tests_flottabilite(date_test)",
        [],
    )?;

    Ok(conn)
}

pub fn get_connection(db_path: &str) -> Result<Connection> {
    Connection::open(db_path)
}

// ==================== GILET CRUD ====================

pub fn get_all_gilets(conn: &Connection) -> Result<Vec<Gilet>> {
    let mut stmt = conn.prepare(
        "SELECT id, num_serie, modele, taille, date_achat, etat, dernier_test FROM gilets"
    )?;
    
    let gilets = stmt.query_map([], |row| {
        Ok(Gilet {
            id: row.get(0)?,
            num_serie: row.get(1)?,
            modele: row.get(2)?,
            taille: row.get(3)?,
            date_achat: row.get(4)?,
            etat: row.get(5)?,
            dernier_test: row.get(6)?,
        })
    })?;
    
    gilets.collect()
}

pub fn get_gilet_by_id(conn: &Connection, id: i64) -> Result<Option<Gilet>> {
    let mut stmt = conn.prepare(
        "SELECT id, num_serie, modele, taille, date_achat, etat, dernier_test FROM gilets WHERE id = ?"
    )?;
    
    stmt.query_row([id], |row| {
        Ok(Gilet {
            id: row.get(0)?,
            num_serie: row.get(1)?,
            modele: row.get(2)?,
            taille: row.get(3)?,
            date_achat: row.get(4)?,
            etat: row.get(5)?,
            dernier_test: row.get(6)?,
        })
    }).map(Some).or_else(|e| {
        if e == rusqlite::Error::QueryReturnedNoRows {
            Ok(None)
        } else {
            Err(e)
        }
    })
}

pub fn create_gilet(conn: &Connection, gilet: &NewGilet) -> Result<Gilet> {
    conn.execute(
        "INSERT INTO gilets (num_serie, modele, taille, date_achat, etat, dernier_test) 
         VALUES (?, ?, ?, ?, ?, ?)",
        (
            &gilet.num_serie,
            &gilet.modele,
            &gilet.taille,
            &gilet.date_achat,
            &gilet.etat,
            &gilet.dernier_test,
        ),
    )?;
    
    let id = conn.last_insert_rowid();
    get_gilet_by_id(conn, id).map(|opt| opt.unwrap())
}

pub fn update_gilet(conn: &Connection, id: i64, gilet: &UpdateGilet) -> Result<Option<Gilet>> {
    // Use a simpler approach with individual updates
    let mut query = "UPDATE gilets SET ".to_string();
    let mut params: Vec<Box<dyn rusqlite::ToSql>> = Vec::new();
    
    let mut has_updates = false;
    
    if let Some(ref num_serie) = gilet.num_serie {
        if has_updates { query.push_str(", "); }
        query.push_str("num_serie = ?");
        params.push(Box::new(num_serie.clone()));
        has_updates = true;
    }
    if let Some(ref modele) = gilet.modele {
        if has_updates { query.push_str(", "); }
        query.push_str("modele = ?");
        params.push(Box::new(modele.clone()));
        has_updates = true;
    }
    if let Some(ref taille) = gilet.taille {
        if has_updates { query.push_str(", "); }
        query.push_str("taille = ?");
        params.push(Box::new(taille.clone()));
        has_updates = true;
    }
    if let Some(ref date_achat) = gilet.date_achat {
        if has_updates { query.push_str(", "); }
        query.push_str("date_achat = ?");
        params.push(Box::new(date_achat.clone()));
        has_updates = true;
    }
    if let Some(ref etat) = gilet.etat {
        if has_updates { query.push_str(", "); }
        query.push_str("etat = ?");
        params.push(Box::new(etat.clone()));
        has_updates = true;
    }
    if let Some(ref dernier_test) = gilet.dernier_test {
        if has_updates { query.push_str(", "); }
        query.push_str("dernier_test = ?");
        params.push(Box::new(dernier_test.clone()));
        has_updates = true;
    }
    
    if !has_updates {
        return Ok(None);
    }
    
    query.push_str(" WHERE id = ?");
    
    // Build parameter list
    let mut all_params: Vec<&dyn rusqlite::ToSql> = params.iter().map(|p| p.as_ref()).collect();
    all_params.push(&id);
    
    // Use a match to handle up to 10 parameters (rusqlite limit for tuple params)
    match all_params.len() {
        1 => conn.execute(&query, [all_params[0]])?,
        2 => conn.execute(&query, [all_params[0], all_params[1]])?,
        3 => conn.execute(&query, [all_params[0], all_params[1], all_params[2]])?,
        4 => conn.execute(&query, [all_params[0], all_params[1], all_params[2], all_params[3]])?,
        5 => conn.execute(&query, [all_params[0], all_params[1], all_params[2], all_params[3], all_params[4]])?,
        6 => conn.execute(&query, [all_params[0], all_params[1], all_params[2], all_params[3], all_params[4], all_params[5]])?,
        7 => conn.execute(&query, [all_params[0], all_params[1], all_params[2], all_params[3], all_params[4], all_params[5], all_params[6]])?,
        _ => return Err(rusqlite::Error::InvalidParameterCount(0, 0)),
    };
    
    get_gilet_by_id(conn, id)
}

pub fn delete_gilet(conn: &Connection, id: i64) -> Result<bool> {
    let rows = conn.execute("DELETE FROM gilets WHERE id = ?", [id])?;
    Ok(rows > 0)
}

pub fn search_gilets(conn: &Connection, field: &str, value: &str) -> Result<Vec<Gilet>> {
    let valid_fields = ["num_serie", "modele", "taille", "etat"];
    if !valid_fields.contains(&field) {
        return Err(rusqlite::Error::InvalidQuery);
    }
    
    let query = format!("SELECT id, num_serie, modele, taille, date_achat, etat, dernier_test FROM gilets WHERE {} = ?", field);
    
    let mut stmt = conn.prepare(&query)?;
    let gilets = stmt.query_map([value], |row| {
        Ok(Gilet {
            id: row.get(0)?,
            num_serie: row.get(1)?,
            modele: row.get(2)?,
            taille: row.get(3)?,
            date_achat: row.get(4)?,
            etat: row.get(5)?,
            dernier_test: row.get(6)?,
        })
    })?;
    
    gilets.collect()
}

// ==================== TEST CRUD ====================

pub fn get_all_tests(conn: &Connection) -> Result<Vec<TestFlottabilite>> {
    let mut stmt = conn.prepare(
        "SELECT id, gilet_id, date_test, resultat, commentaire, responsable FROM tests_flottabilite"
    )?;
    
    let tests = stmt.query_map([], |row| {
        Ok(TestFlottabilite {
            id: row.get(0)?,
            gilet_id: row.get(1)?,
            date_test: row.get(2)?,
            resultat: row.get(3)?,
            commentaire: row.get(4)?,
            responsable: row.get(5)?,
        })
    })?;
    
    tests.collect()
}

pub fn get_test_by_id(conn: &Connection, id: i64) -> Result<Option<TestFlottabilite>> {
    let mut stmt = conn.prepare(
        "SELECT id, gilet_id, date_test, resultat, commentaire, responsable FROM tests_flottabilite WHERE id = ?"
    )?;
    
    stmt.query_row([id], |row| {
        Ok(TestFlottabilite {
            id: row.get(0)?,
            gilet_id: row.get(1)?,
            date_test: row.get(2)?,
            resultat: row.get(3)?,
            commentaire: row.get(4)?,
            responsable: row.get(5)?,
        })
    }).map(Some).or_else(|e| {
        if e == rusqlite::Error::QueryReturnedNoRows {
            Ok(None)
        } else {
            Err(e)
        }
    })
}

pub fn create_test(conn: &Connection, test: &NewTestFlottabilite) -> Result<TestFlottabilite> {
    conn.execute(
        "INSERT INTO tests_flottabilite (gilet_id, date_test, resultat, commentaire, responsable) 
         VALUES (?, ?, ?, ?, ?)",
        (
            &test.gilet_id,
            &test.date_test,
            &test.resultat,
            &test.commentaire,
            &test.responsable,
        ),
    )?;
    
    let id = conn.last_insert_rowid();
    get_test_by_id(conn, id).map(|opt| opt.unwrap())
}

pub fn update_test(conn: &Connection, id: i64, test: &UpdateTestFlottabilite) -> Result<Option<TestFlottabilite>> {
    let mut query = "UPDATE tests_flottabilite SET ".to_string();
    let mut params: Vec<Box<dyn rusqlite::ToSql>> = Vec::new();
    
    let mut has_updates = false;
    
    if let Some(gilet_id) = test.gilet_id {
        if has_updates { query.push_str(", "); }
        query.push_str("gilet_id = ?");
        params.push(Box::new(gilet_id));
        has_updates = true;
    }
    if let Some(date_test) = test.date_test {
        if has_updates { query.push_str(", "); }
        query.push_str("date_test = ?");
        params.push(Box::new(date_test));
        has_updates = true;
    }
    if let Some(ref resultat) = test.resultat {
        if has_updates { query.push_str(", "); }
        query.push_str("resultat = ?");
        params.push(Box::new(resultat.clone()));
        has_updates = true;
    }
    if let Some(ref commentaire) = test.commentaire {
        if has_updates { query.push_str(", "); }
        query.push_str("commentaire = ?");
        params.push(Box::new(commentaire.clone()));
        has_updates = true;
    }
    if let Some(ref responsable) = test.responsable {
        if has_updates { query.push_str(", "); }
        query.push_str("responsable = ?");
        params.push(Box::new(responsable.clone()));
        has_updates = true;
    }
    
    if !has_updates {
        return Ok(None);
    }
    
    query.push_str(" WHERE id = ?");
    
    let mut all_params: Vec<&dyn rusqlite::ToSql> = params.iter().map(|p| p.as_ref()).collect();
    all_params.push(&id);
    
    match all_params.len() {
        1 => conn.execute(&query, [all_params[0]])?,
        2 => conn.execute(&query, [all_params[0], all_params[1]])?,
        3 => conn.execute(&query, [all_params[0], all_params[1], all_params[2]])?,
        4 => conn.execute(&query, [all_params[0], all_params[1], all_params[2], all_params[3]])?,
        5 => conn.execute(&query, [all_params[0], all_params[1], all_params[2], all_params[3], all_params[4]])?,
        6 => conn.execute(&query, [all_params[0], all_params[1], all_params[2], all_params[3], all_params[4], all_params[5]])?,
        _ => return Err(rusqlite::Error::InvalidParameterCount(0, 0)),
    };
    
    get_test_by_id(conn, id)
}

pub fn delete_test(conn: &Connection, id: i64) -> Result<bool> {
    let rows = conn.execute("DELETE FROM tests_flottabilite WHERE id = ?", [id])?;
    Ok(rows > 0)
}

pub fn get_tests_by_gilet(conn: &Connection, gilet_id: i64) -> Result<Vec<TestFlottabilite>> {
    let mut stmt = conn.prepare(
        "SELECT id, gilet_id, date_test, resultat, commentaire, responsable 
         FROM tests_flottabilite WHERE gilet_id = ? ORDER BY date_test DESC"
    )?;
    
    let tests = stmt.query_map([gilet_id], |row| {
        Ok(TestFlottabilite {
            id: row.get(0)?,
            gilet_id: row.get(1)?,
            date_test: row.get(2)?,
            resultat: row.get(3)?,
            commentaire: row.get(4)?,
            responsable: row.get(5)?,
        })
    })?;
    
    tests.collect()
}

pub fn get_gilet_with_tests(conn: &Connection, gilet_id: i64) -> Result<Option<(Gilet, Vec<TestFlottabilite>)>> {
    let gilet = match get_gilet_by_id(conn, gilet_id)? {
        Some(g) => g,
        None => return Ok(None),
    };
    
    let tests = get_tests_by_gilet(conn, gilet_id)?;
    Ok(Some((gilet, tests)))
}
