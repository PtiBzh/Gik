use chrono::NaiveDate;
use std::str::FromStr;

pub fn parse_date(date_str: &str) -> Result<NaiveDate, String> {
    NaiveDate::from_str(date_str)
        .map_err(|e| format!("Format de date invalide: {}", e))
}

pub fn format_date(date: &NaiveDate) -> String {
    date.format("%Y-%m-%d").to_string()
}

pub fn validate_gilet_data(
    num_serie: &str,
    modele: &str,
    taille: &str,
    etat: &str,
) -> Result<(), String> {
    if num_serie.is_empty() {
        return Err("Le numéro de série ne peut pas être vide".to_string());
    }
    if modele.is_empty() {
        return Err("Le modèle ne peut pas être vide".to_string());
    }
    if taille.is_empty() {
        return Err("La taille ne peut pas être vide".to_string());
    }
    if etat.is_empty() {
        return Err("L'état ne peut pas être vide".to_string());
    }
    Ok(())
}

pub fn validate_test_data(
    resultat: &str,
    responsable: &str,
) -> Result<(), String> {
    if resultat != "OK" && resultat != "KO" {
        return Err("Le résultat doit être 'OK' ou 'KO'".to_string());
    }
    if responsable.is_empty() {
        return Err("Le responsable ne peut pas être vide".to_string());
    }
    Ok(())
}
