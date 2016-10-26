extern crate rand;
extern crate time;
use rand::{thread_rng, Rng};
use time::precise_time_ns;
use std::io::prelude::*;
use std::fs::File;
use std::thread;

const NUM_LOCKERS: usize = 150;
const TIME_TO_CHANGE: usize = 30; // 5 * 6 = 30
const RUNTIME: usize = 3600;
const CUSTOMER_PROBABILITY: u8 = 1;
const CUSTOMER_PROBABILITY_MAX: u8 = 10;
static NTHREADS: i32 = 10;

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

    pub fn free_locker(&mut self) {
        self.state = LockerState::Free;
        self.return_time = 0;
        self.reset_encounter();
//        println!("Locker {} free", self.id)
    }

    pub fn use_locker(&mut self) {
        self.state = LockerState::InUse;
    }

    pub fn is_free(&self) -> bool {
        self.state == LockerState::Free
    }

    pub fn update_locker(&mut self, time: i16, occupied_lockers: &mut i16) {
        if !self.is_free() {
            if time == self.return_time - (TIME_TO_CHANGE as i16) {
                self.use_locker();
            } else if time == self.return_time {
                self.free_locker();
                *occupied_lockers = *occupied_lockers - 1;
            } else if time == self.occupy_time + (TIME_TO_CHANGE as i16) {
                self.occupy_locker();
                self.reset_encounter();
            }
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

fn update_lockers(lr: &mut [Locker], time: i16, occupied_lockers: &mut i16) {
    for mut x in lr.iter_mut() {
        x.update_locker(time, &mut *occupied_lockers);
    }}

fn check_new_customer() -> bool {
    let mut rng = thread_rng();
    let cst: u8 = rng.gen_range(1, CUSTOMER_PROBABILITY_MAX);
    cst == CUSTOMER_PROBABILITY
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

fn new_customer(lr: &mut [Locker], time: i16, occupied_lockers: &mut i16, input_data: &Vec<i16>) {
    if *occupied_lockers == (NUM_LOCKERS as i16) {
        println!("Error: All lockers occupied")
    } else {
        let locker_number = get_random_free_locker(lr);
        lr[(locker_number as usize)].assign_locker(time, time+get_return_time(input_data));
        *occupied_lockers = *occupied_lockers + 1;
//        println!("New customer in locker {}, free at {}, occupied lockers {}", locker_number, lr[(locker_number as usize)].return_time, occupied_lockers)
    }

}

fn has_encounter(lr: &mut [Locker], encounters: &mut i16, a: usize, b: usize) {
    if !lr[a].is_free() && !lr[b].is_free() {
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

fn simulation(input_data: &Vec<i16>) {
    let mut locker_array: [Locker; NUM_LOCKERS] = [Locker { id: 0, return_time: 0, occupy_time: 0, state: LockerState::Free, had_encounter: false}; NUM_LOCKERS];
    initialize_lockers(&mut locker_array);
    let mut occupied_lockers: i16 = 0;
    let mut customers: i16 = 0;
    let mut encounters: i16 = 0;

    let mut i = 0;
    while i < (RUNTIME as i16) {
//        println!("Update {}", i);
        update_lockers(&mut locker_array, i, &mut occupied_lockers);
        if check_new_customer() {
            customers = customers + 1;
            new_customer(&mut locker_array, i, &mut occupied_lockers, input_data)
        }
        detect_encounters(&mut locker_array, &mut encounters);
        i = i + 1;
    }

//    println!("Total customers: {}", customers);
//    println!("Total encounters: {}", encounters);
    print!("{}", format!("Total customers {}\n", customers));
    print!("{}", format!("Total encounters {}\n", encounters));
}


fn main() {
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
                input_data.push(split2[0].trim().parse().ok().expect("err"));
            }
        }
    }

    let mut children = vec![];
    let tm3 = precise_time_ns();
    //    while i < 10000 {
    //        simulation(&input_data);
    //        i = i + 1;
    //    }
    for _ in 0..NTHREADS {
        let i_data = input_data.clone();
        children.push(thread::spawn(move || {
            let mut j = 0;
            while j < 1000 {
                simulation(&i_data.clone());
                j = j + 1;
            }
        }));
    }
    for child in children {
        // Wait for the thread to finish. Returns a result.
        let _ = child.join();
    }
    let tm4 = precise_time_ns();
    println!("{}ns", tm4-tm3);
}
