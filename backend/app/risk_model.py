import json
import math
from dataclasses import dataclass
from functools import lru_cache
from random import Random

from .models import Status


def features(history):
    longest_run = current_run = 0
    for status in history:
        current_run = current_run + 1 if status == Status.absent else 0
        longest_run = max(longest_run, current_run)
    late = history.count(Status.late)
    return (history.count(Status.present) + late) / len(history), longest_run, late


def synthetic_examples():
    rng = Random(25)
    examples = []
    for _ in range(1000):
        absence_rate = rng.uniform(0.03, 0.65)
        lateness_rate = rng.uniform(0.03, 0.22)
        weights = [1 - absence_rate - lateness_rate, absence_rate, lateness_rate]
        history = rng.choices([Status.present, Status.absent, Status.late], weights, k=24)
        future_absences = sum(rng.random() < absence_rate for _ in range(8))
        examples.append((features(history), int(future_absences >= 4)))
    rng.shuffle(examples)
    return examples


def sigmoid(value):
    return 1 / (1 + math.exp(-max(-50, min(50, value))))


def scaled(values):
    attendance, streak, lateness = values
    return 1.0, attendance, streak / 8, lateness / 8


@dataclass(frozen=True)
class LogisticModel:
    weights: tuple[float, ...]

    def predict(self, values):
        return sigmoid(sum(weight * value for weight, value in zip(self.weights, scaled(values))))


def fit(examples):
    rows = [(scaled(values), label) for values, label in examples]
    weights = [0.0] * 4
    for _ in range(350):
        gradient = [0.0] * 4
        for row, label in rows:
            error = sigmoid(sum(weight * value for weight, value in zip(weights, row))) - label
            for index, value in enumerate(row):
                gradient[index] += error * value
        for index, value in enumerate(gradient):
            weights[index] -= 0.8 * value / len(rows)
    return LogisticModel(tuple(weights))


def evaluate(model, examples):
    outcomes = [(label, model.predict(values) >= 0.5) for values, label in examples]
    true_positive = sum(label and prediction for label, prediction in outcomes)
    false_positive = sum(not label and prediction for label, prediction in outcomes)
    false_negative = sum(label and not prediction for label, prediction in outcomes)
    f1_denominator = 2 * true_positive + false_positive + false_negative
    return {
        "accuracy": round(sum(label == prediction for label, prediction in outcomes) / len(outcomes), 4),
        "f1": round(2 * true_positive / f1_denominator, 4) if f1_denominator else 0.0,
        "test_examples": len(outcomes),
    }


@lru_cache(maxsize=1)
def trained_model():
    examples = synthetic_examples()
    training, holdout = examples[:800], examples[800:]
    model = fit(training)
    return model, evaluate(model, holdout)


if __name__ == "__main__":
    _, metrics = trained_model()
    print(json.dumps(metrics))
