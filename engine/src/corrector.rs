pub fn spatial_substitution_cost(c1: char, c2: char) -> f32 {
    if c1 == c2 { return 0.0; }
    
    // Flat matching eliminates string allocation and searching
    let is_neighbor = match c1 {
        'q' => matches!(c2, 'w' | 'a'),
        'w' => matches!(c2, 'q' | 'e' | 'a' | 's'),
        'e' => matches!(c2, 'w' | 'r' | 's' | 'd'),
        'r' => matches!(c2, 'e' | 't' | 'd' | 'f'),
        't' => matches!(c2, 'r' | 'y' | 'f' | 'g'),
        'y' => matches!(c2, 't' | 'u' | 'g' | 'h'),
        'u' => matches!(c2, 'y' | 'i' | 'j' | 'h'),
        'i' => matches!(c2, 'u' | 'o' | 'k' | 'j'),
        'o' => matches!(c2, 'i' | 'p' | 'l' | 'k'),
        'p' => matches!(c2, 'o' | 'l'),
        'a' => matches!(c2, 'q' | 'w' | 's' | 'z'),
        's' => matches!(c2, 'w' | 'e' | 'a' | 'd' | 'z' | 'x'),
        'd' => matches!(c2, 'e' | 'r' | 's' | 'f' | 'x' | 'c'),
        'f' => matches!(c2, 'r' | 't' | 'd' | 'g' | 'c' | 'v'),
        'g' => matches!(c2, 't' | 'y' | 'f' | 'h' | 'v' | 'b'),
        'h' => matches!(c2, 'y' | 'u' | 'g' | 'j' | 'b' | 'n'),
        'j' => matches!(c2, 'u' | 'i' | 'h' | 'k' | 'n' | 'm'),
        'k' => matches!(c2, 'i' | 'o' | 'j' | 'l' | 'm'),
        'l' => matches!(c2, 'o' | 'p' | 'k'),
        'z' => matches!(c2, 'a' | 's' | 'x'),
        'x' => matches!(c2, 's' | 'd' | 'z' | 'c'),
        'c' => matches!(c2, 'd' | 'f' | 'x' | 'v'),
        'v' => matches!(c2, 'f' | 'g' | 'c' | 'b'),
        'b' => matches!(c2, 'g' | 'h' | 'v' | 'n'),
        'n' => matches!(c2, 'h' | 'j' | 'b' | 'm'),
        'm' => matches!(c2, 'j' | 'k' | 'n'),
        _ => false,
    };

    if is_neighbor { 0.3 } else { 1.0 }
}

/// Zero-allocation early exit spatial distance
pub fn spatial_edit_distance_early_exit(a: &str, b: &str, max_dist: f32) -> f32 {
    let a_bytes = a.as_bytes();
    let b_bytes = b.as_bytes();
    let a_len = a_bytes.len();
    let b_len = b_bytes.len();

    if a_len == 0 { return b_len as f32; }
    if b_len == 0 { return a_len as f32; }

    if (a_len as isize - b_len as isize).unsigned_abs() as f32 > max_dist {
        return max_dist + 1.0;
    }

    // Reusable stack arrays prevent heap allocation thrashing on Android
    let mut prev = [0.0f32; 32];
    let mut curr = [0.0f32; 32];
    let limit = b_len.min(31); 

    for j in 0..=limit { prev[j] = j as f32; }

    for i in 1..=a_len {
        curr[0] = i as f32;
        let mut row_min = curr[0];

        for j in 1..=limit {
            let c1 = a_bytes[i - 1] as char;
            let c2 = b_bytes[j - 1] as char;
            let sub_cost = spatial_substitution_cost(c1, c2);

            let del = prev[j] + 1.0;
            let ins = curr[j - 1] + 1.0;
            let sub = prev[j - 1] + sub_cost;

            curr[j] = del.min(ins).min(sub);
            if curr[j] < row_min { row_min = curr[j]; }
        }

        if row_min > max_dist { return max_dist + 1.0; }
        prev[..=limit].copy_from_slice(&curr[..=limit]);
    }
    curr[limit]
}
