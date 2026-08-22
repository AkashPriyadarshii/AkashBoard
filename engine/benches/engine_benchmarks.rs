/*
 * engine_benchmarks.rs — Performance benchmarks for AkashBoard's prediction engine.
 *
 * Uses criterion.rs to measure:
 * - predict() latency at various vocabulary sizes
 * - correct() latency at various dictionary sizes
 * - learn() latency
 * - edit_distance() latency for various string lengths
 *
 * Run: cargo bench
 */

use criterion::{black_box, criterion_group, criterion_main, Criterion, BenchmarkId};
use akashboard_engine::predictor::{Predictor, edit_distance};
use akashboard_engine::corrector::Corrector;
use akashboard_engine::learner::Learner;

// ════════════════════════════════════════════════════════════════════════
// PREDICT BENCHMARKS
// ════════════════════════════════════════════════════════════════════════

fn bench_predict(c: &mut Criterion) {
    let mut group = c.benchmark_group("predict");

    for vocab_size in [100, 1_000, 10_000] {
        let mut p = Predictor::new();
        for i in 0..vocab_size {
            p.learn(&format!("word{}", i), &format!("ctx{}", i % 10));
        }

        group.bench_with_input(
            BenchmarkId::new("top5", vocab_size),
            &vocab_size,
            |b, &size| {
                b.iter(|| {
                    black_box(p.predict(&format!("ctx{}", size % 10), 5));
                });
            },
        );
    }

    group.finish();
}

fn bench_predict_various_k(c: &mut Criterion) {
    let mut group = c.benchmark_group("predict_topk");
    let mut p = Predictor::new();
    for i in 0..1000 {
        p.learn(&format!("word{}", i), "common context");
    }

    for k in [1, 3, 5] {
        group.bench_with_input(BenchmarkId::new("k", k), &k, |b, &k_val| {
            b.iter(|| {
                black_box(p.predict("common context", k_val));
            });
        });
    }

    group.finish();
}

// ════════════════════════════════════════════════════════════════════════
// CORRECT BENCHMARKS
// ════════════════════════════════════════════════════════════════════════

fn bench_correct(c: &mut Criterion) {
    let mut group = c.benchmark_group("correct");

    for dict_size in [100, 1_000, 10_000] {
        let mut cr = Corrector::new();
        for i in 0..dict_size {
            cr.add_known_word(format!("word{}", i));
        }

        group.bench_with_input(
            BenchmarkId::new("known_word", dict_size),
            &dict_size,
            |b, &size| {
                b.iter(|| {
                    black_box(cr.correct(&format!("wor{}", size % 100)));
                });
            },
        );
    }

    group.finish();
}

fn bench_correct_common_typos(c: &mut Criterion) {
    let mut grp = c.benchmark_group("correct_typos");
    let cr = Corrector::new();

    let typos = [("teh", "the"), ("adn", "and"), ("taht", "that"), ("hte", "the")];

    for (typo, _expected) in &typos {
        let typo_str = typo.to_string();
        grp.bench_function(
            BenchmarkId::new("typo", typo_str.clone()),
            |b| {
                b.iter(|| {
                    black_box(cr.correct(&typo_str));
                });
            },
        );
    }

    grp.finish();
}

// ════════════════════════════════════════════════════════════════════════
// LEARN BENCHMARKS
// ════════════════════════════════════════════════════════════════════════

fn bench_learn(c: &mut Criterion) {
    let mut l = Learner::new();
    c.bench_function("learn_word", |b| {
        let mut i = 0u64;
        b.iter(|| {
            black_box(l.learn_word(&format!("bench_word{}", i), "context", i));
            i += 1;
        });
    });
}

fn bench_learn_decay(c: &mut Criterion) {
    c.bench_function("apply_decay_1000_patterns", |b| {
        b.iter_batched(
            || {
                let mut l = Learner::new();
                for i in 0..1000 {
                    l.learn_word(&format!("w{}", i), "", i * 1000);
                }
                l
            },
            |mut l| {
                black_box(l.apply_decay(1_000_000, 30));
            },
            criterion::BatchSize::SmallInput,
        );
    });
}

// ════════════════════════════════════════════════════════════════════════
// EDIT DISTANCE BENCHMARKS
// ════════════════════════════════════════════════════════════════════════

fn bench_edit_distance(c: &mut Criterion) {
    let mut group = c.benchmark_group("edit_distance");

    let pairs = vec![
        ("hello".to_string(), "helo".to_string()),
        ("keyboard".to_string(), "keybaord".to_string()),
        ("akashboard".to_string(), "akashboad".to_string()),
        ("short".to_string(), "hort".to_string()),
        ("a".repeat(50), "b".repeat(50)),
        ("a".repeat(200), "b".repeat(200)),
    ];

    for (a, b) in &pairs {
        let label = if a.len() > 10 {
            format!("len_{}", a.len())
        } else {
            format!("{}_to_{}", a, b)
        };
        let a_clone = a.clone();
        let b_clone = b.clone();
        group.bench_with_input(
            BenchmarkId::new("distance", label),
            &(a_clone, b_clone),
            |b, (aa, bb)| {
                b.iter(|| {
                    black_box(edit_distance(aa, bb));
                });
            },
        );
    }

    group.finish();
}

// ════════════════════════════════════════════════════════════════════════
// INTEGRATION WORKFLOW BENCHMARKS
// ════════════════════════════════════════════════════════════════════════

fn bench_typing_workflow(c: &mut Criterion) {
    c.bench_function("full_typing_cycle", |b| {
        b.iter_batched(
            || {
                let mut p = Predictor::new();
                let mut cr = Corrector::new();
                let mut l = Learner::new();
                // Pre-populate
                for i in 0..5000 {
                    p.learn(&format!("word{}", i), "common ctx");
                    cr.add_known_word(format!("word{}", i));
                    l.learn_word(&format!("word{}", i), "common ctx", i * 100);
                }
                (p, cr, l)
            },
            |(mut p, mut cr, mut l)| {
                // Simulate typing "helo wrld"
                let predictions = p.predict("common ctx", 5);
                black_box(predictions);

                let corrected = cr.correct("helo");
                black_box(corrected);

                l.learn_word("helo", "common ctx", 500000);
                black_box(l.pattern_count());
            },
            criterion::BatchSize::SmallInput,
        );
    });
}

// ════════════════════════════════════════════════════════════════════════
// Criterion Groups
// ════════════════════════════════════════════════════════════════════════

criterion_group!(
    benches,
    bench_predict,
    bench_predict_various_k,
    bench_correct,
    bench_correct_common_typos,
    bench_learn,
    bench_learn_decay,
    bench_edit_distance,
    bench_typing_workflow,
);

criterion_main!(benches);
