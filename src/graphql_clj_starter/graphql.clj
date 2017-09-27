(ns graphql-clj-starter.graphql
  (:require [graphql-clj.resolver :as resolver]
            [graphql-clj.executor :as executor]
            [graphql-clj.query-validator :as qv]
            [graphql-clj.schema-validator :as sv]
            [clojure.core.match :as match]
            [clojure.tools.logging :as log]))

(def schema
  "


")

(def starter-schema "# enum for episode
  enum Episode { NEWHOPE, EMPIRE, JEDI }

# interface for Character
interface Character {
  id: String!
  name: String
  friends: [Character]
  appearsIn: [Episode]
}

# human implements character
type Human implements Character {
  id: String!
  name: String
  # friends of human
  friends: [Character]
  appearsIn: [Episode]
  homePlanet: String
}

type Droid implements Character {
  id: String!
  name: String
  friends: [Character]
  appearsIn: [Episode]
  primaryFunction: String
}

# Root Query
type Query {
  # return hero from a particular episode
  hero(episode: Episode): Character  
  human(id: String): [Human]
  # humans: [Human]
  humans(ids: [String!]): [Human!]
  droid(id: String): [Droid]
  droids(ids: [String!]): [Droid!]
  hello(world: WorldInput): String
  objectList: [Object!]!
  nestedInput(nested: NestedInput): String
  characters: [Character]
}

type Object {
  id: String!
}

input WorldInput {
  text: String
}

input NestedInput {
  value: WorldInput
}

type Mutation {
  # create human for given name, it accepts list of friends as variable
  createHuman(name: String, friends: [String]): Human
}

schema {
  query: Query
  mutation: Mutation
}")

(def luke {:id "1000",
           :name "Luke Skywalker"
           :friends ["1002" "1003" "2000" "2001" ]
           :appearsIn [ 4, 5, 6 ],
           :homePlanet "Tatooine"})

(def vader {:id "1001",
            :name "Darth Vader"
            :friends [ "1004" ]
            :appearsIn [ 4, 5, 6 ]
            :homePlanet "Tatooine"})

(def han {
          :id "1002",
          :name "Han Solo",
          :friends [ "1000", "1003", "2001" ],
          :appearsIn [ 4, 5, 6 ],
})

(def leia {
           :id "1003",
           :name "Leia Organa",
           :friends [ "1000", "1002", "2000", "2001" ],
           :appearsIn [ 4, 5, 6 ],
           :homePlanet "Alderaan",
})

(def tarkin {
             :id "1004",
             :name "Wilhuff Tarkin",
             :friends [ "1001" ],
             :appearsIn [ 4 ],
             })

(def humanData  (atom {
                       "1000" luke
                       "1001" vader
                       "1002" han
                       "1003" leia
                       "1004" tarkin}))

(def threepio {
               :id "2000",
               :name "C-3PO",
               :friends [ "1000", "1002", "1003", "2001" ],
               :appearsIn [ 4, 5, 6 ],
               :primaryFunction "Protocol",
               })

(def artoo {
            :id "2001",
            :name "R2-D2",
            :friends [ "1000", "1002", "1003" ],
            :appearsIn [ 4, 5, 6 ],
            :primaryFunction "Astromech",
            })

(def droidData (atom {"2000" threepio
                      "2001" artoo}))

(def all-characters [luke vader han leia tarkin threepio artoo])

(def all-humans [luke vader han leia tarkin])

(def all-droids [threepio artoo])

(defn get-human [id]
  (if (not (nil? id))
    (get @humanData (str id))
    all-humans)) ; BUG: String should be parsed as string instead of int

(defn get-droid [id]
  (if (not (nil? id))
    (get @droidData (str id))
    all-droids)) ; BUG: String should be parsed as string instead of int

(defn get-character [id]
  (or (get-human id) ; BUG: String should be parsed as string instead of int
      (get-droid id)))

(comment (defn get-characters []
   (let [humans (into {} @humanData)
         droids (into {} @droidData)]
     (merge humans
            droids))))

(defn get-characters []
  all-characters)

(defn get-friends [character]
  (map get-character (:friends character)))

(defn get-hero [episode]
  (if (= episode 5)
    luke
    artoo))

(def human-id (atom 2050))

(defn create-human [args]
  (let [new-human-id (str (swap! human-id inc))
        new-human {:id new-human-id
                   :name (get args "name")
                   :friends (get args "friends")}]
    (swap! humanData assoc new-human-id new-human)
    new-human))

(defn starter-resolver-fn [type-name field-name]
  (match/match
   [type-name field-name]
   ["Query" "characters"] (fn [context parent args]
                            (get-characters))
   ["Query" "hero"] (fn [context parent args]
                      (get-hero (:episode args)))
   ["Query" "human"] (fn [context parent args]
                       (let [id (str (get args "id"))
                             human (if (and id
                                            (not (clojure.string/blank? id)))
                                     [(get-human id)]
                                     (get-human nil))]
                         human))
   ["Query" "humans"] (fn [context parent args]
                        (let [ids-list (get args "ids")]
                          (map #(get-human (str %)) ids-list)))
   ["Query" "droid"] (fn [context parent args]
                       (let [id (str (get args "id"))
                             droid (if (and id
                                            (not (clojure.string/blank? id)))
                                     [(get-droid id)]
                                     (get-droid nil))]
                         droid))
   ["Query" "droids"] (fn [context parent args]
                        (let [ids-list (get args "ids")]
                          (map #(get-droid (str %)) ids-list)))
   ["Query" "objectList"] (fn [context parent args]
                            (repeat 3 {:id (java.util.UUID/randomUUID)}))
   ;; Hacky!!! Should use resolver for interface
   ["Human" "friends"] (fn [context parent args]
                         (get-friends parent))
   ["Droid" "friends"] (fn [context parent args]
                         (get-friends parent))
   ["Character" "friends"] (fn [context parent args]
                             (get-friends parent))
   ["Mutation" "createHuman"] (fn [context parent args]
                                (create-human args))
   ["Query" "hello"] (fn [context parent args]
                       (let [world (get args "world")]
                         world))
   :else nil))

(def validated-schema (sv/validate-schema starter-schema))

(defn execute
  [query variables operation-name]
  (executor/execute nil validated-schema starter-resolver-fn query variables operation-name))

