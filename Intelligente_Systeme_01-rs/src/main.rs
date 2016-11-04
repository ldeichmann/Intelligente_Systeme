extern crate rand;
extern crate time;
use rand::{thread_rng, Rng};
use time::precise_time_ns;
use std::io;
use std::io::prelude::*;
use std::fs::File;
use std::thread;
use std::sync::{Arc, Mutex};
use std::collections::HashMap;

const ALGO: usize = 1; // 0 = random - 1 = distributed
const EXIT_AFTER_FOCUS: bool = false;
const RUNS: usize = 10000;
const NTHREADS: usize = 8;
const NUM_LOCKERS: usize = 150;
const TIME_TO_CHANGE: usize = 30; // 5 * 6 = 30
const MINUTES_TO_UNITS_FACTOR: i16 = 6;
const RUNTIME: usize = 4320;
//const PROB_THRESHOLD: i16 = 0;
const CUSTOMER_PROBABILITY: u8 = 10;
const CUSTOMER_PROBABILITY_MAX: u8 = 100;
const FOCUS_BEGIN: i16 = 1770;
const FOCUS_END: i16 = 1830;

#[derive(Clone, Debug, Copy, PartialEq)]
enum LockerState {
    Free,
    InUse,
    Occupied,
}

#[derive(Clone, Debug, Copy)]
struct Locker {
    id: i16,
    return_time: i16,
    occupy_time: i16,
    state: LockerState,
    had_encounter: bool,
    focus: bool,
    focus_counted: bool,
    encounter_probability: f32,
}

// impl of Val
impl Locker {
    pub fn assign_locker(&mut self, time: i16, return_time: i16) {
        self.use_locker();
        self.occupy_time = time;
        self.return_time = return_time;
    }

    pub fn occupy_locker(&mut self) {
        self.state = LockerState::Occupied;
    }

    pub fn reset_encounter(&mut self) {
        self.had_encounter = false;
    }

    pub fn reset_focus_counted(&mut self) {
        self.focus_counted = false;
    }

    pub fn free_locker(&mut self) {
        self.state = LockerState::Free;
        self.return_time = 0;
        self.reset_encounter();
        self.reset_focus_counted();
        self.focus = false;
//        println!("Locker {} free", self.id)
    }

    pub fn use_locker(&mut self) {
        self.state = LockerState::InUse;
        self.encounter_probability = 1.0;
    }

    pub fn is_free(&self) -> bool {
        self.state == LockerState::Free
    }

    pub fn is_in_use(&self) -> bool {
        self.state == LockerState::InUse
    }

    pub fn update_probability(&mut self, time: i16, prob_map: &HashMap<i16, f32>) {
        if self.is_free() {
            self.encounter_probability = 0.0_f32;
            return;
        }

        let time_to_check = time-self.occupy_time;
        if self.is_in_use() {
            self.encounter_probability = 1.0;
        } else if prob_map.contains_key(&(time_to_check)) {
            self.encounter_probability = *prob_map.get(&(time_to_check)).unwrap();
        }
    }

    pub fn update_locker(&mut self, time: i16, occupied_lockers: &mut i16, prob_map: &HashMap<i16, f32>) {
        if !self.is_free() {
            // customer returns
            if time == self.return_time - (TIME_TO_CHANGE as i16) {
                self.use_locker();
            //customer leaves the gym
            } else if time == self.return_time {
                self.free_locker();
                *occupied_lockers = *occupied_lockers - 1;
            // customer leaves for the gym
            } else if time == self.occupy_time + (TIME_TO_CHANGE as i16) {
                self.occupy_locker();
                self.reset_encounter();
                self.reset_focus_counted();
            }
            self.update_probability(time, prob_map);
        }
    }
}

fn initialize_lockers(lr: &mut [Locker]) {
    let mut created_lockers: i16 = 0;
    for mut x in lr.iter_mut() {
        x.id = created_lockers;
        created_lockers = created_lockers + 1;
    }
}

fn update_lockers(lr: &mut [Locker], time: i16, occupied_lockers: &mut i16, prob_map: &HashMap<i16, f32>) {
    for mut x in lr.iter_mut() {
        x.update_locker(time, &mut *occupied_lockers, prob_map);
    }}

fn check_new_customer() -> bool {
    let mut rng = thread_rng();
    let cst: u8 = rng.gen_range(1, CUSTOMER_PROBABILITY_MAX+1);
    cst <= CUSTOMER_PROBABILITY
}

fn get_free_locker(lr: &[Locker]) -> i16 {
    let mut min_val = 200.0_f32;
    let mut min_pos = 0;
    for x in 0..(lr.len()-1 as usize) {
        if lr[x].is_free() {
            let mut sum_prob = 0.0_f32;
            if x == 0 {
                sum_prob = lr[x+1].encounter_probability;
                sum_prob = sum_prob + lr[x+2].encounter_probability;
                sum_prob = sum_prob + lr[x+3].encounter_probability;
            } else if x == 1 {
                sum_prob = lr[x-1].encounter_probability;
                sum_prob = sum_prob + lr[x+1].encounter_probability;
                sum_prob = sum_prob + lr[x+2].encounter_probability;
            } else if x == NUM_LOCKERS-1 {
                sum_prob = lr[x-1].encounter_probability;
                sum_prob = sum_prob + lr[x-2].encounter_probability;
                sum_prob = sum_prob + lr[x-3].encounter_probability;
            } else if x == NUM_LOCKERS-2 {
                sum_prob = lr[x+1].encounter_probability;
                sum_prob = sum_prob + lr[x-1].encounter_probability;
                sum_prob = sum_prob + lr[x-2].encounter_probability;
            } else if (x % 2) == 0 {
                sum_prob = lr[x-2].encounter_probability;
                sum_prob = sum_prob + lr[x-1].encounter_probability;
                sum_prob = sum_prob + lr[x+1].encounter_probability;
                sum_prob = sum_prob + lr[x+2].encounter_probability;
                sum_prob = sum_prob + lr[x+3].encounter_probability;
            } else if (x % 2) == 1 {
                sum_prob = lr[x-3].encounter_probability;
                sum_prob = sum_prob + lr[x-2].encounter_probability;
                sum_prob = sum_prob + lr[x-1].encounter_probability;
                sum_prob = sum_prob + lr[x+1].encounter_probability;
                sum_prob = sum_prob + lr[x+2].encounter_probability;
            } else {
                println!("Sit your bird ass down!")
            }
            if sum_prob == 0.0_f32 {
                return x as i16
            } else if sum_prob <= min_val {
                min_pos = x;
                min_val = sum_prob;
            }

        }
    }
    min_pos as i16
}

fn get_random_free_locker(lr: &[Locker]) -> i16 {
    let mut rng = thread_rng();
    let nr: i16 = rng.gen_range(0, lr.len() as i16);
//    println!("Get Random free locker {} {}", nr, lr[nr as usize].is_free());
    if !lr[nr as usize].is_free() {
        return get_random_free_locker(&lr)
    } else {
        return nr
    }
}

fn get_return_time(input_data: &Vec<i16>) -> i16 {
    let mut rng = thread_rng();
    let nr: usize = rng.gen_range(0, input_data.len()-1);
    input_data[nr]
}

fn new_customer(lr: &mut [Locker], time: i16, occupied_lockers: &mut i16, input_data: &Vec<i16>) -> i16 {
    if *occupied_lockers == (NUM_LOCKERS as i16) {
        println!("Error: All lockers occupied");
        -1
    } else {
        let mut locker_number: i16 = -1;
        if ALGO == 0 {
            locker_number = get_random_free_locker(lr);
        } else if ALGO == 1 {
            locker_number = get_free_locker(lr);
        }
        lr[(locker_number as usize)].assign_locker(time, time+get_return_time(input_data));
        *occupied_lockers = *occupied_lockers + 1;
        locker_number
//        println!("New customer in locker {}, free at {}, occupied lockers {}", locker_number, lr[(locker_number as usize)].return_time, occupied_lockers)
    }

}

fn has_encounter(lr: &mut [Locker], encounters: &mut i16, a: usize, b: usize) {
    if lr[a].is_in_use() && lr[b].is_in_use() {
        if !lr[a].had_encounter || !lr[b].had_encounter {
            *encounters = *encounters + 1;
            lr[a].had_encounter = true;
            lr[b].had_encounter = true;
        }
    }
}

fn detect_encounters(lr: &mut [Locker], encounters: &mut i16) {
    for x in 0..(lr.len()-1 as usize) {
        if !lr[x].is_free() {
            if x == 0 {
                has_encounter(&mut *lr, &mut *encounters, x, x+1);
                has_encounter(&mut *lr, &mut *encounters, x, x+2);
                has_encounter(&mut *lr, &mut *encounters, x, x+3);
            } else if x == 1 {
                has_encounter(&mut *lr, &mut *encounters, x, x-1);
                has_encounter(&mut *lr, &mut *encounters, x, x+1);
                has_encounter(&mut *lr, &mut *encounters, x, x+2);
            } else if x == NUM_LOCKERS-1 {
                has_encounter(&mut *lr, &mut *encounters, x, x-1);
                has_encounter(&mut *lr, &mut *encounters, x, x-2);
                has_encounter(&mut *lr, &mut *encounters, x, x-3);
            } else if x == NUM_LOCKERS-2 {
                has_encounter(&mut *lr, &mut *encounters, x, x+1);
                has_encounter(&mut *lr, &mut *encounters, x, x-1);
                has_encounter(&mut *lr, &mut *encounters, x, x-2);
            } else if (x % 2) == 0 {
                has_encounter(&mut *lr, &mut *encounters, x, x-2);
                has_encounter(&mut *lr, &mut *encounters, x, x-1);
                has_encounter(&mut *lr, &mut *encounters, x, x+1);
                has_encounter(&mut *lr, &mut *encounters, x, x+2);
                has_encounter(&mut *lr, &mut *encounters, x, x+3);
            } else if (x % 2) == 1 {
                has_encounter(&mut *lr, &mut *encounters, x, x-3);
                has_encounter(&mut *lr, &mut *encounters, x, x-2);
                has_encounter(&mut *lr, &mut *encounters, x, x-1);
                has_encounter(&mut *lr, &mut *encounters, x, x+1);
                has_encounter(&mut *lr, &mut *encounters, x, x+2);
            } else {
                println!("This is why you don't freeze time, you guys. It's incredibly irresponsible.")
            }
        }
    }
}

fn detect_focus_encounter(lr: &mut [Locker], id: &i16, encounters: &mut i16) {
    let id = *id as usize;
    if lr[id].had_encounter && !lr[id].focus_counted {
        lr[id].focus_counted = true;
        *encounters = *encounters + 1;
    }
}

fn simulation(input_data: &Vec<i16>, prob_map: &HashMap<i16, f32>) -> (i16, i16, i16){
    let mut locker_array: [Locker; NUM_LOCKERS] =   [Locker {
                                                            id: 0,
                                                            return_time: 0,
                                                            occupy_time: 0,
                                                            state: LockerState::Free,
                                                            had_encounter: false,
                                                            focus: false,
                                                            focus_counted: false,
                                                            encounter_probability: 0.0_f32
                                                            };
                                                            NUM_LOCKERS
                                                   ];
    initialize_lockers(&mut locker_array);
    let mut occupied_lockers: i16 = 0;
    let mut customers: i16 = 0;
    let mut encounters: i16 = 0;
    let mut focus_locker_id: i16 = -1;
    let mut focus_encounters: i16 = 0;

    let mut i = 0;
    while i < (RUNTIME as i16) {
        update_lockers(&mut locker_array, i, &mut occupied_lockers, prob_map);
        if check_new_customer() {
            customers = customers + 1;
            let id: i16 = new_customer(&mut locker_array, i, &mut occupied_lockers, input_data);
            if FOCUS_BEGIN <= i && focus_locker_id == -1 && id != -1 {
                focus_locker_id = id;
                locker_array[focus_locker_id as usize].focus = true;
            }
        }
        // if we were really unlucky and didn't get a focus person, force it
        if i > FOCUS_END && focus_locker_id == -1 {
            focus_locker_id = new_customer(&mut locker_array, i, &mut occupied_lockers, input_data);
            if focus_locker_id != -1 {
                locker_array[focus_locker_id as usize].focus = true;
            }
        }
        if EXIT_AFTER_FOCUS {
            if i > FOCUS_END && focus_locker_id != -1 {
                if !locker_array[focus_locker_id as usize].focus {
                    return (customers, encounters, focus_encounters);
                }
            }
        }
        detect_encounters(&mut locker_array, &mut encounters);
        if focus_locker_id != -1 && locker_array[focus_locker_id as usize].focus {
            detect_focus_encounter(&mut locker_array, &focus_locker_id, &mut focus_encounters)
        }
        i = i + 1;
    }

//    print!("{}", format!("Total customers {}\n", customers));
//    print!("{}", format!("Total encounters {}\n", encounters));
    (customers, encounters, focus_encounters)
}

fn parse_belegungszeiten() -> Vec<i16> {
    let mut data = String::new();
    let mut f = match File::open("Belegungszeiten.txt") {
        Ok(file) => file,
        Err(_) => {
            panic!("Could not read Belegungszeiten")
        }
    };
    f.read_to_string(&mut data).expect("could not read to string");
    let split = data.as_str().split("\n");
    let mut vec = split.collect::<Vec<&str>>();
    vec.remove(0);

    let mut input_data: Vec<i16> = Vec::new();

    for x in &vec {
        if x.trim().len() > 1 {
            let split2 = x.split(" ").collect::<Vec<&str>>();
            let my_int: usize = split2[1].trim().parse().ok().expect("err");
            for _ in 0..my_int - 1 {
                let data: i16 = split2[0].trim().parse().ok().expect("err");
                input_data.push(data * MINUTES_TO_UNITS_FACTOR);
            }
        }
    }
    input_data
}

fn gen_prob_map(input_data: &Vec<i16>) -> HashMap<i16, f32>{

    let mut prob_map = HashMap::new();
    let mut tmp: i16 = input_data[0];
    let mut count: f32 = 0.0;
    for x in input_data {
        if *x != tmp {
            prob_map.insert(tmp, count);
            tmp = *x;
            //            count = 1.0_f32;
        }
        count = count + 1.0_f32;
    }
    prob_map.insert(tmp, count);
//    println!("{:?}", prob_map);

    prob_map = prob_map.iter()
        .map(|(k, v)| (*k,(v/(count as f32))))
        .collect();
//    println!("{:?}", prob_map);
    prob_map
}

fn write_results(results: Arc<Mutex<Vec<(i16, i16, i16)>>>) -> Result<(), io::Error> {
    let mut hello = String::from("customers,encounters,focus_encounters\n");
    let m = results.lock().unwrap();

    for a in m.iter() {
        hello.push_str(&format!("{},{},{}\n", a.0, a.1, a.2));
    }

    let mut f = try!(File::create("results.csv"));
    try!(f.write_all(hello.as_bytes()));
    Ok(())
}

fn main() {

    let input_data = parse_belegungszeiten();
    // generate probability hashmap
    let prob_map = gen_prob_map(&input_data);


    let mut children = vec![];
    let results_vec: Vec<(i16, i16, i16)> = vec![];
    let results_container = Arc::new(Mutex::new(results_vec));
    let tm3 = precise_time_ns();
    for _ in 0..NTHREADS {
        let i_data = input_data.clone();
        let prob_map_clone = prob_map.clone();
        let total_data = results_container.clone();
        children.push(thread::spawn(move || {
            let mut j = 0;
            let mut results: Vec<(i16, i16, i16)> = vec![];
            while j < RUNS/NTHREADS {
                results.push(simulation(&i_data.clone(), &prob_map_clone.clone() ));
                j = j + 1;
            }
            let mut total_data = total_data.lock().unwrap();
            total_data.append(&mut results);
        }));
    }
    for child in children {
        // Wait for the thread to finish. Returns a result.
        let _ = child.join();
    }
    let tm4 = precise_time_ns();
    let results = results_container.clone();
    let mut sum_cust = 0.0_f32;
    let mut sum_enc = 0.0_f32;
    let mut sum_foc_enc = 0.0_f32;
    {
        let m = results.lock().unwrap();
        for a in m.iter() {
            sum_cust = sum_cust + a.0 as f32;
            sum_enc = sum_enc + a.1 as f32;
            sum_foc_enc = sum_foc_enc + a.2 as f32;
        }
    }
    let _ = write_results(results_container.clone());
    println!("{}: {}\n{}: {}ms\n{}:\n\t{}: {}\n\t{}: {}\n\t{}: {}\n\t{}: {}",
             "Runs",
             RUNS,
             "Runtime",
             (tm4-tm3)/1000000,
             "Per day",
             "Average customers",
             sum_cust/(RUNS as f32),
             "Average encounters",
             sum_enc/(RUNS as f32),
             "Average focus person encounters",
             sum_foc_enc/(RUNS as f32),
             "Average focus person encounters for 10 days",
             (sum_foc_enc*10.0)/(RUNS as f32))
}
